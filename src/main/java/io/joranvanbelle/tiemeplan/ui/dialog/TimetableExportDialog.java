package io.joranvanbelle.tiemeplan.ui.dialog;

import io.joranvanbelle.tiemeplan.service.export.ExportAudience;
import io.joranvanbelle.tiemeplan.service.export.ExportFilters;
import io.joranvanbelle.tiemeplan.service.export.ExportFormat;
import io.joranvanbelle.tiemeplan.service.export.ExportSelection;
import io.joranvanbelle.tiemeplan.service.export.ScenarioSnapshot;
import io.joranvanbelle.tiemeplan.service.export.TimetableExportRequest;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class TimetableExportDialog {

    private final ScenarioSnapshot snapshot;
    private final ComboBox<ExportFormat> formatCombo = new ComboBox<>();
    private final ComboBox<ExportAudience> audienceCombo = new ComboBox<>();
    private final ComboBox<ExportSelection> selectionCombo = new ComboBox<>();
    private final ComboBox<String> entityCombo = new ComboBox<>();
    private final CheckBox applyFilters = new CheckBox("Apply current filters");
    private final Spinner<Integer> pngScale = new Spinner<>(1, 4, 2);
    private final CheckBox useCurrentTheme = new CheckBox("Use current theme");
    private final Label noteLabel = new Label();
    private final Label errorLabel = new Label();

    public TimetableExportDialog(ScenarioSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Optional<TimetableExportRequest> showAndWait(Window owner) {
        Dialog<TimetableExportRequest> dialog = new Dialog<>();
        dialog.setTitle("Export timetable");
        dialog.setHeaderText("Choose the format, content, and destination.");
        if (owner != null) {
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
        }
        dialog.setResizable(true);
        dialog.getDialogPane().getStyleClass().add("export-dialog");

        ButtonType exportType = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exportType, ButtonType.CANCEL);

        configureControls();
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(8, 4, 4, 4));
        form.addRow(0, new Label("Format"), formatCombo);
        form.addRow(1, new Label("Content"), audienceCombo);
        form.addRow(2, new Label("Which"), selectionCombo);
        form.addRow(3, new Label("Entity"), entityCombo);
        form.addRow(4, new Label("Filters"), applyFilters);
        form.addRow(5, new Label("PNG scale"), pngScale);
        form.addRow(6, new Label("PNG theme"), useCurrentTheme);
        form.add(noteLabel, 0, 7, 2, 1);
        form.add(errorLabel, 0, 8, 2, 1);
        dialog.getDialogPane().setContent(form);

        noteLabel.setWrapText(true);
        noteLabel.setMaxWidth(420);
        noteLabel.getStyleClass().add("export-note");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(420);
        errorLabel.getStyleClass().add("export-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Node exportButton = dialog.getDialogPane().lookupButton(exportType);
        exportButton.getStyleClass().add("primary-button");
        exportButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validate();
            if (error != null) {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> button == exportType ? buildRequest() : null);
        updateOptions();
        return dialog.showAndWait();
    }

    private void configureControls() {
        formatCombo.getItems().setAll(ExportFormat.values());
        formatCombo.setValue(ExportFormat.PDF);
        formatCombo.setConverter(enumConverter(ExportFormat::name));
        formatCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateOptions());

        audienceCombo.getItems().setAll(ExportAudience.values());
        audienceCombo.setValue(ExportAudience.MASTER);
        audienceCombo.setConverter(enumConverter(ExportAudience::displayName));
        audienceCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            rebuildEntityItems();
            updateOptions();
        });

        selectionCombo.getItems().setAll(ExportSelection.values());
        selectionCombo.setValue(ExportSelection.ALL);
        selectionCombo.setConverter(enumConverter(ExportSelection::displayName));
        selectionCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateOptions());

        entityCombo.setPrefWidth(260);
        entityCombo.setPromptText("Select an entity");
        rebuildEntityItems();

        applyFilters.setSelected(true);
        applyFilters.setDisable(true);
        applyFilters.setTooltip(new Tooltip("Timetable filters are introduced with the timetable toolbar."));
        pngScale.setEditable(false);
        pngScale.setPrefWidth(90);
        useCurrentTheme.setSelected(true);
    }

    private void rebuildEntityItems() {
        ExportAudience audience = audienceCombo.getValue();
        List<String> names = switch (audience == null ? ExportAudience.MASTER : audience) {
            case MASTER -> List.of();
            case TEACHER -> snapshot.teachers().stream()
                    .map(Teacher::value)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            case STUDENT_GROUP -> snapshot.studentGroups().stream()
                    .map(StudentGroup::value)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            case CLASSROOM -> snapshot.classrooms().stream()
                    .map(Classroom::name)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        };
        entityCombo.getItems().setAll(names);
        if (entityCombo.getValue() != null && !names.contains(entityCombo.getValue())) {
            entityCombo.setValue(null);
        }
    }

    private void updateOptions() {
        ExportAudience audience = audienceCombo.getValue();
        boolean master = audience == null || audience == ExportAudience.MASTER;
        if (master) {
            selectionCombo.setValue(ExportSelection.ALL);
            selectionCombo.setDisable(true);
        } else {
            selectionCombo.setDisable(false);
        }
        boolean single = !master && selectionCombo.getValue() == ExportSelection.SINGLE;
        entityCombo.setDisable(!single);

        boolean png = formatCombo.getValue() == ExportFormat.PNG;
        pngScale.setDisable(!png);
        useCurrentTheme.setDisable(!png);

        if (selectionCombo.getValue() == ExportSelection.ALL) {
            if (master) {
                noteLabel.setText("PNG exports a single image. PDF and CSV produce one combined file.");
            } else {
                noteLabel.setText("PDF is one combined multi-page file; CSV is one combined file; "
                        + "PNG is a ZIP of one image per teacher, student group, or classroom.");
            }
        } else {
            noteLabel.setText("A single entity is exported. PNG produces a direct .png file.");
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private String validate() {
        ExportAudience audience = audienceCombo.getValue();
        if (audience == null) {
            return "Choose what to export.";
        }
        if (formatCombo.getValue() == null) {
            return "Choose an export format.";
        }
        if (audience != ExportAudience.MASTER
                && selectionCombo.getValue() == ExportSelection.SINGLE
                && (entityCombo.getValue() == null || entityCombo.getValue().isBlank())) {
            return "Select a specific " + audience.name().toLowerCase().replace('_', ' ') + ".";
        }
        return null;
    }

    private TimetableExportRequest buildRequest() {
        ExportAudience audience = audienceCombo.getValue();
        ExportSelection selection = audience == ExportAudience.MASTER
                ? ExportSelection.ALL
                : selectionCombo.getValue();
        String entity = selection == ExportSelection.SINGLE ? entityCombo.getValue() : null;
        return new TimetableExportRequest(
                formatCombo.getValue(),
                audience,
                selection,
                entity,
                ExportFilters.none(),
                applyFilters.isSelected(),
                pngScale.getValue(),
                useCurrentTheme.isSelected());
    }

    private static <T> StringConverter<T> enumConverter(java.util.function.Function<T, String> toString) {
        return new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : toString.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }
}
