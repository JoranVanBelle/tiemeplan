package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.backtrack.ScheduledCourse;
import io.joranvanbelle.tiemeplan.service.export.ExportException;
import io.joranvanbelle.tiemeplan.service.export.ExportFileNames;
import io.joranvanbelle.tiemeplan.service.export.ExportResult;
import io.joranvanbelle.tiemeplan.service.export.ExportService;
import io.joranvanbelle.tiemeplan.service.export.ExportTheme;
import io.joranvanbelle.tiemeplan.service.export.ScenarioSnapshot;
import io.joranvanbelle.tiemeplan.service.export.TimetableExportRequest;
import io.joranvanbelle.tiemeplan.ui.dialog.Dialogs;
import io.joranvanbelle.tiemeplan.ui.dialog.TimetableExportDialog;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Timeslot;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class TimetableView implements ViewProvider {

    private static final String LAST_DIRECTORY_KEY = "lastExportDirectory";

    private final NavigationService navigationService;
    private final ScenarioViewModel scenarioViewModel;
    private final Supplier<BacktrackResult> resultSupplier;
    private final ExportService exportService;
    private final Supplier<ExportTheme> exportThemeSupplier;
    private final Preferences preferences = Preferences.userNodeForPackage(TimetableView.class);

    private Button exportButton;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    private Button cancelExportButton;
    private Task<ExportResult> activeTask;

    public TimetableView() {
        this(null, new ScenarioViewModel(), () -> null, new ExportService(), () -> ExportTheme.LIGHT);
    }

    public TimetableView(NavigationService navigationService) {
        this(navigationService, new ScenarioViewModel(), () -> null, new ExportService(), () -> ExportTheme.LIGHT);
    }

    public TimetableView(
            NavigationService navigationService,
            ScenarioViewModel scenarioViewModel,
            Supplier<BacktrackResult> resultSupplier,
            ExportService exportService,
            Supplier<ExportTheme> exportThemeSupplier) {
        this.navigationService = navigationService;
        this.scenarioViewModel = scenarioViewModel;
        this.resultSupplier = resultSupplier;
        this.exportService = exportService;
        this.exportThemeSupplier = exportThemeSupplier;
    }

    @Override
    public Node getView() {
        abortExport();
        VBox root = new VBox(16);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(24));

        VBox headerText = new VBox(4);
        headerText.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Timetable");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Scheduled courses by timeslot and classroom.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        headerText.getChildren().addAll(titleLabel, subtitleLabel);

        BacktrackResult result = resultSupplier.get();
        switch (result) {
            case BacktrackResult.Success(ClassSchedule solution) -> {
                exportButton = new Button("Export");
                exportButton.getStyleClass().add("primary-button");
                exportButton.setTooltip(new Tooltip("Export the timetable as PDF, CSV, or PNG."));
                exportButton.setAccessibleText("Export timetable");
                exportButton.setOnAction(event -> openExportDialog(solution));
                root.getChildren().addAll(headerWithAction(headerText, exportButton), createStatusRow(),
                        createGrid(solution));
                return root;
            }
            case BacktrackResult.NoSolutionFound _ -> {
                root.getChildren().addAll(header(headerText), noSolutionCard());
                return root;
            }
            case BacktrackResult.Cancelled _ -> {
                root.getChildren().addAll(header(headerText), cancelledCard());
                return root;
            }
            case null, default -> {
                root.getChildren().addAll(header(headerText), noTimetableCard());
                return root;
            }
        }
    }

    private HBox header(VBox headerText) {
        HBox header = new HBox(12, headerText);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox headerWithAction(VBox headerText, Button action) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, headerText, spacer, action);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createStatusRow() {
        statusLabel = new Label();
        statusLabel.getStyleClass().add("export-status");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(18, 18);
        progressIndicator.setMaxSize(18, 18);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);

        cancelExportButton = new Button("Cancel export");
        cancelExportButton.getStyleClass().add("ghost-button");
        cancelExportButton.setVisible(false);
        cancelExportButton.setManaged(false);
        cancelExportButton.setOnAction(event -> {
            if (activeTask != null) {
                activeTask.cancel();
            }
        });

        HBox row = new HBox(10, progressIndicator, statusLabel, cancelExportButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox noSolutionCard() {
        return card("No solution found",
                "The solver could not find a valid timetable for the current setup. Check constraints and available timeslots/classrooms.",
                true);
    }

    private VBox cancelledCard() {
        return card("Solving cancelled", "The solving process was cancelled.", false);
    }

    private VBox noTimetableCard() {
        return card("No timetable yet",
                "Configure your scenario in Setup and Courses, then run Solve from the Constraints screen.",
                true);
    }

    private VBox card(String title, String message, boolean withNavigationActions) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("card-subtitle");
        messageLabel.setWrapText(true);
        card.getChildren().addAll(titleLabel, messageLabel);
        if (withNavigationActions) {
            card.getChildren().add(navigationActions());
        }
        return card;
    }

    private HBox navigationActions() {
        HBox actions = new HBox(10);
        Button setup = new Button("Go to Setup");
        setup.getStyleClass().add("secondary-button");
        setup.setOnAction(e -> navigate(Screen.SETUP));
        Button courses = new Button("Go to Courses");
        courses.getStyleClass().add("secondary-button");
        courses.setOnAction(e -> navigate(Screen.COURSES));
        Button constraints = new Button("Go to Constraints");
        constraints.getStyleClass().add("primary-button");
        constraints.setOnAction(e -> navigate(Screen.CONSTRAINTS));
        actions.getChildren().addAll(setup, courses, constraints);
        return actions;
    }

    private Node createGrid(ClassSchedule schedule) {
        List<Timeslot> timeslots = schedule.assignedCourses().stream()
                .map(ScheduledCourse::timeslot)
                .distinct()
                .sorted(Comparator.comparing(Timeslot::dayOfWeek).thenComparing(Timeslot::startTime))
                .toList();
        List<Classroom> classrooms = schedule.assignedCourses().stream()
                .map(ScheduledCourse::classroom)
                .distinct()
                .sorted(Comparator.comparing(Classroom::name))
                .toList();

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        grid.add(new Label("Timeslot"), 0, 0);
        for (int c = 0; c < classrooms.size(); c++) {
            Label cl = new Label(classrooms.get(c).name());
            cl.getStyleClass().add("card-title");
            grid.add(cl, c + 1, 0);
        }

        for (int r = 0; r < timeslots.size(); r++) {
            Timeslot ts = timeslots.get(r);
            VBox timeBox = new VBox(2);
            Label day = new Label(ts.dayOfWeek().toString());
            Label time = new Label(ts.startTime().toString());
            day.getStyleClass().add("timetable-time-day");
            time.getStyleClass().add("timetable-time");
            timeBox.getChildren().addAll(day, time);
            timeBox.getStyleClass().add("timetable-time-cell");
            grid.add(timeBox, 0, r + 1);
            for (int c = 0; c < classrooms.size(); c++) {
                Classroom cr = classrooms.get(c);
                Node cell = createCell(schedule, ts, cr);
                grid.add(cell, c + 1, r + 1);
            }
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.getStyleClass().add("timetable-scroll");
        return sp;
    }

    private Node createCell(ClassSchedule schedule, Timeslot ts, Classroom cr) {
        for (ScheduledCourse sc : schedule.assignedCourses()) {
            if (sc.timeslot().equals(ts) && sc.classroom().equals(cr)) {
                VBox cell = new VBox(4);
                cell.getStyleClass().add("timetable-cell");
                Label subject = new Label(sc.course().subject().value());
                subject.getStyleClass().add("timetable-subject");
                Label teacher = new Label(sc.course().teacher().value());
                teacher.getStyleClass().add("timetable-teacher");
                String groups = sc.course().studentGroups().stream()
                        .map(g -> g.value())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                if (!groups.isEmpty()) {
                    Label grp = new Label(groups);
                    grp.getStyleClass().add("timetable-groups");
                    grp.setWrapText(true);
                    cell.getChildren().addAll(subject, teacher, grp);
                } else {
                    cell.getChildren().addAll(subject, teacher);
                }
                return cell;
            }
        }
        Label empty = new Label("");
        empty.getStyleClass().add("timetable-cell-empty");
        return empty;
    }

    private void openExportDialog(ClassSchedule schedule) {
        ScenarioSnapshot snapshot = ScenarioSnapshot.from(scenarioViewModel);
        TimetableExportDialog dialog = new TimetableExportDialog(snapshot);
        Optional<TimetableExportRequest> request = dialog.showAndWait(window());
        if (request.isEmpty()) {
            return;
        }
        Path target = chooseTarget(request.get());
        if (target == null) {
            return;
        }
        startExport(snapshot, schedule, request.get(), target);
    }

    private Path chooseTarget(TimetableExportRequest request) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export timetable");
        chooser.setInitialFileName(ExportFileNames.suggest(request, Instant.now(), ZoneId.systemDefault()));
        chooser.getExtensionFilters().add(extensionFilter(request));
        String lastDirectory = preferences.get(LAST_DIRECTORY_KEY, null);
        if (lastDirectory != null) {
            File directory = new File(lastDirectory);
            if (directory.isDirectory()) {
                chooser.setInitialDirectory(directory);
            }
        }
        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return null;
        }
        if (file.getParentFile() != null) {
            preferences.put(LAST_DIRECTORY_KEY, file.getParentFile().getAbsolutePath());
        }
        Path target = ExportFileNames.ensureExtension(file.toPath(), ExportFileNames.extension(request));
        if (Files.exists(target) && !confirmOverwrite(target)) {
            return null;
        }
        return target;
    }

    private FileChooser.ExtensionFilter extensionFilter(TimetableExportRequest request) {
        return switch (request.format()) {
            case PDF -> new FileChooser.ExtensionFilter("PDF files", "*.pdf");
            case CSV -> new FileChooser.ExtensionFilter("CSV files", "*.csv");
            case PNG -> request.isBulkPng()
                    ? new FileChooser.ExtensionFilter("ZIP archives", "*.zip")
                    : new FileChooser.ExtensionFilter("PNG images", "*.png");
        };
    }

    private boolean confirmOverwrite(Path target) {
        return Dialogs.confirm(window(), "Replace file?", "A file with this name already exists.",
                target.getFileName() + " will be replaced.");
    }

    private void startExport(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request,
            Path target) {
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        ExportTheme theme = exportThemeSupplier.get();
        Task<ExportResult> task = exportService.exportTask(snapshot, schedule, request, target, locale,
                theme == null ? ExportTheme.LIGHT : theme);
        activeTask = task;

        exportButton.setDisable(true);
        statusLabel.getStyleClass().removeAll("export-status-success", "export-status-error");
        statusLabel.setText("Preparing export...");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);
        progressIndicator.progressProperty().bind(task.progressProperty());
        cancelExportButton.setVisible(true);
        cancelExportButton.setManaged(true);

        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                statusLabel.setText(newValue);
            }
        });
        task.setOnSucceeded(event -> {
            ExportResult result = task.getValue();
            statusLabel.setText("Saved " + result.path().getFileName());
            statusLabel.getStyleClass().add("export-status-success");
            finishExport();
        });
        task.setOnCancelled(event -> {
            statusLabel.setText("Export cancelled.");
            finishExport();
        });
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = userMessage(error);
            statusLabel.setText("Export failed: " + message);
            statusLabel.getStyleClass().add("export-status-error");
            finishExport();
            showExportError(message);
        });

        Thread thread = new Thread(task, "timetable-export");
        thread.setDaemon(true);
        thread.start();
    }

    private void finishExport() {
        progressIndicator.progressProperty().unbind();
        progressIndicator.setProgress(0);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        cancelExportButton.setVisible(false);
        cancelExportButton.setManaged(false);
        exportButton.setDisable(false);
        activeTask = null;
    }

    private String userMessage(Throwable error) {
        if (error == null) {
            return "Unknown error.";
        }
        if (error instanceof ExportException && error.getMessage() != null) {
            return error.getMessage();
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return "The export could not be written.";
        }
        return message;
    }

    private void showExportError(String message) {
        Dialogs.error(window(), "Export failed", message);
    }

    private void abortExport() {
        if (activeTask == null) {
            return;
        }
        Task<ExportResult> task = activeTask;
        activeTask = null;
        task.setOnSucceeded(null);
        task.setOnCancelled(null);
        task.setOnFailed(null);
        task.cancel();
        if (progressIndicator != null && progressIndicator.progressProperty().isBound()) {
            progressIndicator.progressProperty().unbind();
        }
    }

    private Window window() {
        if (exportButton != null && exportButton.getScene() != null) {
            return exportButton.getScene().getWindow();
        }
        return null;
    }

    private void navigate(Screen screen) {
        if (navigationService != null) {
            navigationService.navigateTo(screen);
        }
    }
}
