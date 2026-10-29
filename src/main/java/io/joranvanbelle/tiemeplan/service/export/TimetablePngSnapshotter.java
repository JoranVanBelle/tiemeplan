package io.joranvanbelle.tiemeplan.service.export;

import java.awt.image.BufferedImage;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

public final class TimetablePngSnapshotter {

    private static final double MAX_DIMENSION = 12000;
    private static final double CELL_WIDTH = 168;
    private static final double CELL_HEIGHT = 72;

    public BufferedImage snapshot(
            TimetableExportDocument document,
            TimetableSheet sheet,
            Locale locale,
            ExportTheme theme,
            double scale) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Timetable snapshots must run on the JavaFX Application Thread.");
        }
        VBox root = buildNode(document, sheet, locale, theme);
        new Scene(root);
        root.applyCss();
        root.autosize();
        root.layout();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.web(theme.background()));
        parameters.setTransform(Transform.scale(scale, scale));
        WritableImage image = root.snapshot(parameters, null);
        if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
            throw new ExportException("The timetable is too large to export as a single image ("
                    + (int) image.getWidth() + "×" + (int) image.getHeight() + "). Reduce the scale or use PDF/CSV.");
        }
        return SwingFXUtils.fromFXImage(image, null);
    }

    private VBox buildNode(
            TimetableExportDocument document,
            TimetableSheet sheet,
            Locale locale,
            ExportTheme theme) {
        VBox root = new VBox(8);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: " + theme.background() + ";");

        Label documentTitle = label(document.title(), theme.text(), 18, true);
        Label sheetTitle = label(sheet.title(), theme.text(), 14, true);
        Label subtitle = label(sheet.subtitle(), theme.secondaryText(), 11, false);
        Label filterSummary = label(document.filterSummary(), theme.secondaryText(), 10, false);
        Label generated = label(
                "Generated " + ExportText.timestamp(document.generatedAt(), locale, ZoneId.systemDefault()),
                theme.secondaryText(), 10, false);
        root.getChildren().addAll(documentTitle);
        if (!document.title().equals(sheet.title())) {
            root.getChildren().add(sheetTitle);
        }
        root.getChildren().addAll(subtitle, filterSummary, generated);

        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-background-color: " + theme.border() + ";");
        grid.add(headerCell("Time", theme), 0, 0);
        for (int column = 0; column < sheet.days().size(); column++) {
            DayOfWeek day = sheet.days().get(column);
            grid.add(headerCell(ExportText.dayName(day, locale), theme), column + 1, 0);
        }
        for (int row = 0; row < sheet.rowHeaders().size(); row++) {
            grid.add(timeCell(sheet.rowHeaders().get(row).label(), theme), 0, row + 1);
            for (int column = 0; column < sheet.days().size(); column++) {
                grid.add(cell(sheet.cell(row, column), sheet.audience(), theme), column + 1, row + 1);
            }
        }
        VBox.setVgrow(grid, Priority.NEVER);
        root.getChildren().add(grid);
        for (String note : sheet.notes()) {
            root.getChildren().add(label(note, theme.secondaryText(), 10, false));
        }
        return root;
    }

    private Region headerCell(String text, ExportTheme theme) {
        VBox box = new VBox(label(text, theme.text(), 11, true));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setMinWidth(CELL_WIDTH);
        box.setPrefWidth(CELL_WIDTH);
        box.setMinHeight(30);
        box.setStyle("-fx-background-color: " + theme.surfaceAlt() + ";");
        return box;
    }

    private Region timeCell(String text, ExportTheme theme) {
        VBox box = new VBox(label(text, theme.text(), 11, true));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setMinWidth(CELL_WIDTH);
        box.setPrefWidth(CELL_WIDTH);
        box.setMinHeight(CELL_HEIGHT);
        box.setStyle("-fx-background-color: " + theme.surfaceAlt() + ";");
        return box;
    }

    private Region cell(TimetableCell cell, ExportAudience audience, ExportTheme theme) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(7, 9, 7, 9));
        box.setMinWidth(CELL_WIDTH);
        box.setPrefWidth(CELL_WIDTH);
        box.setMinHeight(CELL_HEIGHT);
        box.setStyle("-fx-background-color: " + background(cell.state(), audience, theme) + ";");
        if (cell.hasCourses()) {
            for (int index = 0; index < cell.entries().size(); index++) {
                if (index > 0) {
                    box.getChildren().add(label("", theme.secondaryText(), 10, false));
                }
                TimetableCellEntry entry = cell.entries().get(index);
                for (String line : entryLines(entry, audience)) {
                    box.getChildren().add(label(line, theme.text(), 10, line.equals(entry.subject())));
                }
            }
            if (cell.state() == SlotState.CONFLICT) {
                box.getChildren().add(label("Conflict", theme.secondaryText(), 10, true));
            }
            return box;
        }
        if (cell.state() == SlotState.BLOCKED) {
            box.getChildren().add(label("Blocked", theme.text(), 10, true));
        } else if (cell.state() == SlotState.FREE && audience == ExportAudience.CLASSROOM) {
            box.getChildren().add(label("Free", theme.secondaryText(), 10, false));
        }
        return box;
    }

    private String background(SlotState state, ExportAudience audience, ExportTheme theme) {
        return switch (state) {
            case COURSE, CONFLICT -> theme.courseSurface();
            case BLOCKED -> theme.blockedSurface();
            case FREE -> audience == ExportAudience.CLASSROOM ? theme.freeSurface() : theme.surface();
            case NOT_A_TIMESLOT -> theme.surface();
        };
    }

    private List<String> entryLines(TimetableCellEntry entry, ExportAudience audience) {
        List<String> lines = new ArrayList<>();
        addIfPresent(lines, entry.subject());
        switch (audience) {
            case MASTER -> {
                addIfPresent(lines, entry.teacher());
                addIfPresent(lines, entry.studentGroupsLabel());
                addIfPresent(lines, entry.classroom());
            }
            case TEACHER -> {
                addIfPresent(lines, entry.studentGroupsLabel());
                addIfPresent(lines, entry.classroom());
            }
            case STUDENT_GROUP -> {
                addIfPresent(lines, entry.teacher());
                addIfPresent(lines, entry.classroom());
            }
            case CLASSROOM -> {
                addIfPresent(lines, entry.studentGroupsLabel());
                addIfPresent(lines, entry.teacher());
            }
        }
        return lines;
    }

    private void addIfPresent(List<String> lines, String value) {
        if (value != null && !value.isBlank()) {
            lines.add(value);
        }
    }

    private Label label(String text, String color, double size, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;"
                + (bold ? " -fx-font-weight: 600;" : ""));
        return label;
    }
}
