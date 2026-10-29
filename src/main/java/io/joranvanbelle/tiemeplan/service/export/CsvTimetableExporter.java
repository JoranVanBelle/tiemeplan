package io.joranvanbelle.tiemeplan.service.export;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class CsvTimetableExporter {

    public static final List<String> COURSE_HEADER = List.of(
            "day", "timeslot", "teacher", "course", "studentgroup", "classroom");
    public static final List<String> OCCUPANCY_HEADER = List.of(
            "day", "timeslot", "classroom", "state", "teacher", "course", "studentgroup");

    private static final Comparator<CsvRow> ROW_ORDER = Comparator
            .comparingInt(CsvRow::dayOrder)
            .thenComparing(CsvRow::time)
            .thenComparing(CsvRow::sortKey);

    public void write(TimetableExportDocument document, Path path, Locale locale) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                CSVWriter csvWriter = new CSVWriter(writer, ';', '"', '"', "\n")) {
            if (document.audience() == ExportAudience.CLASSROOM) {
                writeOccupancy(document, csvWriter, locale);
            } else {
                writeCourses(document, csvWriter, locale);
            }
        }
    }

    private void writeCourses(TimetableExportDocument document, CSVWriter writer, Locale locale) {
        writer.writeNext(COURSE_HEADER.toArray(String[]::new), false);
        List<CsvRow> rows = new ArrayList<>();
        for (TimetableSheet sheet : document.sheets()) {
            for (int rowIndex = 0; rowIndex < sheet.rowHeaders().size(); rowIndex++) {
                RowHeader rowHeader = sheet.rowHeaders().get(rowIndex);
                for (int columnIndex = 0; columnIndex < sheet.days().size(); columnIndex++) {
                    DayOfWeek day = sheet.days().get(columnIndex);
                    TimetableCell cell = sheet.cell(rowIndex, columnIndex);
                    if (!cell.hasCourses()) {
                        continue;
                    }
                    for (TimetableCellEntry entry : cell.entries()) {
                        for (String group : groupsFor(sheet, entry)) {
                            List<String> fields = List.of(
                                    ExportText.dayName(day, locale),
                                    ExportText.time(rowHeader.startTime()),
                                    entry.teacher(),
                                    entry.subject(),
                                    group,
                                    entry.classroom());
                            rows.add(new CsvRow(dayOrder(day), rowHeader.startTime(),
                                    String.join("\u0000", fields), fields));
                        }
                    }
                }
            }
        }
        rows.stream().sorted(ROW_ORDER).forEach(row -> writer.writeNext(row.fields().toArray(String[]::new), false));
    }

    private List<String> groupsFor(TimetableSheet sheet, TimetableCellEntry entry) {
        if (sheet.audience() == ExportAudience.STUDENT_GROUP && sheet.entityName() != null) {
            return List.of(sheet.entityName());
        }
        if (entry.studentGroups().isEmpty()) {
            return List.of("");
        }
        return entry.studentGroups();
    }

    private void writeOccupancy(TimetableExportDocument document, CSVWriter writer, Locale locale) {
        writer.writeNext(OCCUPANCY_HEADER.toArray(String[]::new), false);
        List<CsvRow> rows = new ArrayList<>();
        for (TimetableSheet sheet : document.sheets()) {
            for (int rowIndex = 0; rowIndex < sheet.rowHeaders().size(); rowIndex++) {
                RowHeader rowHeader = sheet.rowHeaders().get(rowIndex);
                for (int columnIndex = 0; columnIndex < sheet.days().size(); columnIndex++) {
                    DayOfWeek day = sheet.days().get(columnIndex);
                    TimetableCell cell = sheet.cell(rowIndex, columnIndex);
                    if (cell.state() == SlotState.NOT_A_TIMESLOT) {
                        continue;
                    }
                    if (cell.hasCourses()) {
                        for (TimetableCellEntry entry : cell.entries()) {
                            List<String> fields = List.of(
                                    ExportText.dayName(day, locale),
                                    ExportText.time(rowHeader.startTime()),
                                    sheet.entityName(),
                                    "COURSE",
                                    entry.teacher(),
                                    entry.subject(),
                                    entry.studentGroupsLabel());
                            rows.add(new CsvRow(dayOrder(day), rowHeader.startTime(),
                                    String.join("\u0000", fields), fields));
                        }
                    } else {
                        List<String> fields = List.of(
                                ExportText.dayName(day, locale),
                                ExportText.time(rowHeader.startTime()),
                                sheet.entityName(),
                                cell.state().name(),
                                "",
                                "",
                                "");
                        rows.add(new CsvRow(dayOrder(day), rowHeader.startTime(),
                                String.join("\u0000", fields), fields));
                    }
                }
            }
        }
        rows.stream().sorted(ROW_ORDER).forEach(row -> writer.writeNext(row.fields().toArray(String[]::new), false));
    }

    private static int dayOrder(DayOfWeek day) {
        return day == null ? 0 : day.getValue();
    }

    private record CsvRow(int dayOrder, LocalTime time, String sortKey, List<String> fields) {
    }
}
