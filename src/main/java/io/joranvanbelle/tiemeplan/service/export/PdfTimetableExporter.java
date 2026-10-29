package io.joranvanbelle.tiemeplan.service.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PdfTimetableExporter {

    private static final Color HEADER_BACKGROUND = new Color(238, 241, 248);
    private static final Color COURSE_BACKGROUND = new Color(240, 249, 255);
    private static final Color BLOCKED_BACKGROUND = new Color(254, 226, 226);
    private static final Color FREE_BACKGROUND = new Color(240, 253, 244);

    public void write(TimetableExportDocument document, Path path, Locale locale) throws IOException {
        try (OutputStream output = Files.newOutputStream(path)) {
            write(document, output, locale);
        }
    }

    public void write(TimetableExportDocument document, OutputStream output, Locale locale) throws IOException {
        Document pdf = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        try {
            PdfWriter.getInstance(pdf, output);
            pdf.open();
            Fonts fonts = loadFonts();
            boolean firstSheet = true;
            for (TimetableSheet sheet : document.sheets()) {
                if (!firstSheet) {
                    pdf.newPage();
                }
                addGridPage(pdf, document, sheet, fonts, locale, firstSheet);
                firstSheet = false;
                if (sheet.audience() != ExportAudience.MASTER) {
                    pdf.newPage();
                    addAgendaPage(pdf, document, sheet, fonts, locale);
                }
            }
            if (document.sheets().isEmpty()) {
                addDocumentHeader(pdf, document, fonts, locale);
                pdf.add(new Paragraph("No entities to export.", fonts.regular()));
            }
            pdf.close();
        } catch (DocumentException e) {
            throw new IOException("Could not create PDF timetable: " + e.getMessage(), e);
        } finally {
            if (pdf.isOpen()) {
                try {
                    pdf.close();
                } catch (RuntimeException ignored) {
                    // Preserve the original failure instead of reporting the empty-document close
                    // error.
                }
            }
        }
    }

    private void addGridPage(
            Document pdf,
            TimetableExportDocument document,
            TimetableSheet sheet,
            Fonts fonts,
            Locale locale,
            boolean firstSheet) throws DocumentException {
        if (firstSheet) {
            addDocumentHeader(pdf, document, fonts, locale);
        }
        pdf.add(title(sheet.title(), fonts.title()));
        pdf.add(new Paragraph(sheet.subtitle(), fonts.subtitle()));
        pdf.add(new Paragraph(document.filterSummary(), fonts.meta()));
        pdf.add(new Paragraph(
                "Generated " + ExportText.timestamp(document.generatedAt(), locale, ZoneId.systemDefault()),
                fonts.meta()));
        pdf.add(new Paragraph(" ", fonts.meta()));

        PdfPTable table = new PdfPTable(sheet.days().size() + 1);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        float[] widths = new float[sheet.days().size() + 1];
        widths[0] = 0.8f;
        for (int index = 1; index < widths.length; index++) {
            widths[index] = 1.0f;
        }
        table.setWidths(widths);

        addHeaderCell(table, "Time", fonts.tableHeader(), HEADER_BACKGROUND);
        for (DayOfWeek day : sheet.days()) {
            addHeaderCell(table, ExportText.dayName(day, locale), fonts.tableHeader(), HEADER_BACKGROUND);
        }

        for (int rowIndex = 0; rowIndex < sheet.rowHeaders().size(); rowIndex++) {
            RowHeader rowHeader = sheet.rowHeaders().get(rowIndex);
            addHeaderCell(table, rowHeader.label(), fonts.tableHeader(), HEADER_BACKGROUND);
            for (int columnIndex = 0; columnIndex < sheet.days().size(); columnIndex++) {
                TimetableCell cell = sheet.cell(rowIndex, columnIndex);
                addBodyCell(table, cell, sheet.audience(), fonts);
            }
        }
        pdf.add(table);
        for (String note : sheet.notes()) {
            pdf.add(new Paragraph(note, fonts.meta()));
        }
    }

    private void addDocumentHeader(
            Document pdf,
            TimetableExportDocument document,
            Fonts fonts,
            Locale locale) throws DocumentException {
        pdf.add(title(document.title(), fonts.documentTitle()));
        pdf.add(new Paragraph(document.filterSummary(), fonts.meta()));
        pdf.add(new Paragraph(
                "Generated " + ExportText.timestamp(document.generatedAt(), locale, ZoneId.systemDefault()),
                fonts.meta()));
        pdf.add(new Paragraph(" ", fonts.meta()));
    }

    private Paragraph title(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(background);
        cell.setPadding(4);
        cell.setMinimumHeight(22);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, TimetableCell cell, ExportAudience audience, Fonts fonts) {
        Paragraph paragraph = new Paragraph();
        paragraph.setLeading(10);
        List<String> lines = cellLines(cell, audience);
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                paragraph.add("\n");
            }
            paragraph.add(new Phrase(lines.get(index), fonts.tableBody()));
        }
        PdfPCell pdfCell = new PdfPCell(paragraph);
        pdfCell.setPadding(4);
        pdfCell.setMinimumHeight(30);
        pdfCell.setVerticalAlignment(Element.ALIGN_TOP);
        pdfCell.setBackgroundColor(backgroundFor(cell.state(), audience));
        table.addCell(pdfCell);
    }

    private Color backgroundFor(SlotState state, ExportAudience audience) {
        return switch (state) {
            case COURSE, CONFLICT -> COURSE_BACKGROUND;
            case BLOCKED -> BLOCKED_BACKGROUND;
            case FREE -> audience == ExportAudience.CLASSROOM ? FREE_BACKGROUND : Color.WHITE;
            case NOT_A_TIMESLOT -> Color.WHITE;
        };
    }

    private List<String> cellLines(TimetableCell cell, ExportAudience audience) {
        List<String> lines = new ArrayList<>();
        if (cell.hasCourses()) {
            for (TimetableCellEntry entry : cell.entries()) {
                if (!lines.isEmpty()) {
                    lines.add("");
                }
                lines.addAll(entryLines(entry, audience));
            }
            if (cell.state() == SlotState.CONFLICT) {
                lines.add("Conflict");
            }
            return lines;
        }
        return switch (cell.state()) {
            case BLOCKED -> List.of("Blocked");
            case FREE -> audience == ExportAudience.CLASSROOM ? List.of("Free") : List.of();
            case NOT_A_TIMESLOT -> List.of();
            case COURSE, CONFLICT -> List.of();
        };
    }

    private List<String> entryLines(TimetableCellEntry entry, ExportAudience audience) {
        return switch (audience) {
            case MASTER -> nonBlank(
                    entry.subject(),
                    entry.teacher(),
                    entry.studentGroupsLabel(),
                    entry.classroom());
            case TEACHER -> nonBlank(
                    entry.subject(),
                    entry.studentGroupsLabel(),
                    entry.classroom());
            case STUDENT_GROUP -> nonBlank(
                    entry.subject(),
                    entry.teacher(),
                    entry.classroom());
            case CLASSROOM -> nonBlank(
                    entry.subject(),
                    entry.studentGroupsLabel(),
                    entry.teacher());
        };
    }

    private List<String> nonBlank(String... values) {
        List<String> lines = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                lines.add(value);
            }
        }
        return lines;
    }

    private void addAgendaPage(
            Document pdf,
            TimetableExportDocument document,
            TimetableSheet sheet,
            Fonts fonts,
            Locale locale) throws DocumentException {
        pdf.add(title("Agenda — " + sheet.title(), fonts.title()));
        pdf.add(new Paragraph(sheet.subtitle(), fonts.subtitle()));
        pdf.add(new Paragraph(document.filterSummary(), fonts.meta()));
        pdf.add(new Paragraph(" ", fonts.meta()));

        boolean anyLine = false;
        for (int columnIndex = 0; columnIndex < sheet.days().size(); columnIndex++) {
            DayOfWeek day = sheet.days().get(columnIndex);
            List<String> dayLines = new ArrayList<>();
            for (int rowIndex = 0; rowIndex < sheet.rowHeaders().size(); rowIndex++) {
                TimetableCell cell = sheet.cell(rowIndex, columnIndex);
                if (cell.state() == SlotState.NOT_A_TIMESLOT) {
                    continue;
                }
                if (sheet.audience() != ExportAudience.CLASSROOM && !cell.hasCourses()) {
                    continue;
                }
                dayLines.add(agendaLine(sheet.rowHeaders().get(rowIndex), cell, sheet.audience()));
            }
            if (!dayLines.isEmpty()) {
                anyLine = true;
                pdf.add(new Paragraph(ExportText.dayName(day, locale), fonts.subtitle()));
                for (String line : dayLines) {
                    pdf.add(new Paragraph(line, fonts.regular()));
                }
                pdf.add(new Paragraph(" ", fonts.meta()));
            }
        }
        if (!anyLine) {
            pdf.add(new Paragraph("No scheduled courses.", fonts.regular()));
        }
        for (String note : sheet.notes()) {
            pdf.add(new Paragraph(note, fonts.meta()));
        }
    }

    private String agendaLine(RowHeader rowHeader, TimetableCell cell, ExportAudience audience) {
        String time = rowHeader.label();
        if (!cell.hasCourses()) {
            return time + "  " + cell.state().name().replace('_', ' ');
        }
        List<String> parts = new ArrayList<>();
        parts.add(time);
        for (TimetableCellEntry entry : cell.entries()) {
            String details = switch (audience) {
                case MASTER -> entry.subject() + " — " + entry.teacher() + " — "
                        + entry.studentGroupsLabel() + " — " + entry.classroom();
                case TEACHER -> entry.subject() + " — " + entry.studentGroupsLabel()
                        + " — " + entry.classroom();
                case STUDENT_GROUP -> entry.subject() + " — " + entry.teacher()
                        + " — " + entry.classroom();
                case CLASSROOM -> "COURSE — " + entry.subject() + " — "
                        + entry.studentGroupsLabel() + " — " + entry.teacher();
            };
            parts.add(details);
        }
        return String.join("  |  ", parts);
    }

    private Fonts loadFonts() throws IOException {
        try {
            byte[] regularBytes = readResource("/fonts/NotoSans-Regular.ttf");
            byte[] boldBytes = readResource("/fonts/NotoSans-Bold.ttf");
            BaseFont regularBase = BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, true, regularBytes, null);
            BaseFont boldBase = BaseFont.createFont("NotoSans-Bold.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, true, boldBytes, null);
            return new Fonts(
                    new Font(regularBase, 9, Font.NORMAL, Color.BLACK),
                    new Font(regularBase, 8, Font.NORMAL, Color.BLACK),
                    new Font(regularBase, 7.5f, Font.NORMAL, Color.DARK_GRAY),
                    new Font(boldBase, 8, Font.BOLD, Color.BLACK),
                    new Font(boldBase, 15, Font.BOLD, Color.BLACK),
                    new Font(boldBase, 12, Font.BOLD, Color.BLACK),
                    new Font(regularBase, 10, Font.NORMAL, Color.BLACK));
        } catch (DocumentException e) {
            throw new IOException("Could not load the bundled Noto Sans font", e);
        }
    }

    private byte[] readResource(String path) throws IOException {
        try (InputStream input = PdfTimetableExporter.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing font resource: " + path);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            input.transferTo(output);
            return output.toByteArray();
        }
    }

    private record Fonts(
            Font regular,
            Font tableBody,
            Font meta,
            Font tableHeader,
            Font documentTitle,
            Font title,
            Font subtitle) {
    }
}
