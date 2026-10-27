package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.service.SolverService;
import io.joranvanbelle.tiemeplan.ui.component.ValidationBanner;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.ValidationIssue;
import io.joranvanbelle.tiemeplan.viewmodel.ValidationSeverity;

import java.util.List;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ConstraintsView implements ViewProvider {
    private final ScenarioViewModel scenarioViewModel;
    private final NavigationService navigationService;
    private final ValidationBanner validationBanner;
    private final SolverService solverService;
    private StackPane overlay;

    public ConstraintsView() {
        this(new ScenarioViewModel(), null);
    }

    public ConstraintsView(ScenarioViewModel scenarioViewModel) {
        this(scenarioViewModel, null);
    }

    public ConstraintsView(ScenarioViewModel scenarioViewModel, NavigationService navigationService) {
        this.scenarioViewModel = scenarioViewModel;
        this.navigationService = navigationService;
        this.solverService = AppState.getInstance().getSolverService();
        this.validationBanner = new ValidationBanner();
        updateValidation();
        this.validationBanner.setOnIssueClick(this::navigateForIssue);
    }

    @Override
    public String getTitle() {
        return "Constraints";
    }

    @Override
    public Node getView() {
        VBox root = new VBox(24);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(32));

        VBox header = new VBox(6);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Constraints & Validation");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Check scenario completeness and run the solver.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        VBox resourcesCard = createCard("Resources", "Classrooms, teachers, subjects, student groups");
        VBox coursesCard = createCard("Courses", "Course definitions and participant mappings");
        VBox timeslotsCard = createCard("Timeslots", "Available time slots");

        Button solveButton = new Button("Solve");
        solveButton.getStyleClass().add("primary-button");
        solveButton.setDisable(hasErrors(scenarioViewModel));
        solveButton.setOnAction(e -> startSolve(solveButton));
        addListeners(solveButton);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button setupBtn = new Button("Go to Setup");
        setupBtn.getStyleClass().add("secondary-button");
        setupBtn.setOnAction(e -> navigateTo(Screen.SETUP));
        Button coursesBtn = new Button("Go to Courses");
        coursesBtn.getStyleClass().add("secondary-button");
        coursesBtn.setOnAction(e -> navigateTo(Screen.COURSES));
        actions.getChildren().addAll(solveButton, setupBtn, coursesBtn);

        VBox content = new VBox(16);
        content.getChildren().addAll(resourcesCard, coursesCard, timeslotsCard, validationBanner, actions);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox main = new VBox(24);
        main.getChildren().addAll(header, scrollPane);

        StackPane stack = new StackPane();
        stack.getChildren().add(main);
        overlay = createOverlay();
        overlay.setVisible(false);
        stack.getChildren().add(overlay);
        return stack;
    }

    private StackPane createOverlay() {
        StackPane overlayPane = new StackPane();
        overlayPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("card");
        box.setMaxWidth(280);
        ProgressIndicator pi = new ProgressIndicator();
        Label lbl = new Label("Solving...");
        lbl.getStyleClass().add("card-title");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("ghost-button");
        cancel.setOnAction(e -> {
            solverService.requestCancel();
        });
        box.getChildren().addAll(pi, lbl, cancel);
        overlayPane.getChildren().add(box);
        return overlayPane;
    }

    private void startSolve(Button solveButton) {
        updateValidation();
        if (hasErrors(scenarioViewModel)) {
            solveButton.setDisable(true);
            return;
        }
        if (solverService.getState() == Worker.State.RUNNING || solverService.getState() == Worker.State.SCHEDULED) {
            return;
        }
        try {
            solverService.setCourses(scenarioViewModel.toDomainCourses());
            solverService.setClassrooms(scenarioViewModel.toDomainClassrooms());
            solverService.setTimeslots(scenarioViewModel.toDomainTimeslots());
        } catch (Exception ex) {
            return;
        }
        overlay.setVisible(true);
        solveButton.setDisable(true);
        solverService.setOnRunning(workerStateEvent -> {
        });
        solverService.setOnSucceeded(e -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            BacktrackResult result = solverService.getValue();
            AppState.getInstance().setLastResult(result);
            if (navigationService != null) {
                navigationService.navigateTo(Screen.TIMETABLE);
            }
        });
        solverService.setOnFailed(e -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            AppState.getInstance().setLastResult(new BacktrackResult.NoSolutionFound(io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.empty()));
            if (navigationService != null) {
                navigationService.navigateTo(Screen.TIMETABLE);
            }
        });
        solverService.setOnCancelled(e -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            AppState.getInstance().setLastResult(new BacktrackResult.Cancelled(io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.empty()));
            if (navigationService != null) {
                navigationService.navigateTo(Screen.TIMETABLE);
            }
        });
        solverService.restart();
    }

    private VBox createCard(String title, String subtitle) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("card-subtitle");
        card.getChildren().addAll(t, s);
        return card;
    }

    private void addListeners(Button solveButton) {
        javafx.beans.InvalidationListener l = obs -> {
            updateValidation();
            if (solverService.getState() == Worker.State.RUNNING) {
                solveButton.setDisable(true);
            } else {
                solveButton.setDisable(hasErrors(scenarioViewModel));
            }
        };
        scenarioViewModel.courses().addListener(l);
        scenarioViewModel.classrooms().addListener(l);
        scenarioViewModel.timeslots().addListener(l);
        scenarioViewModel.teachers().addListener(l);
        scenarioViewModel.subjects().addListener(l);
        scenarioViewModel.studentGroups().addListener(l);
    }

    private void updateValidation() {
        List<ValidationIssue> issues = io.joranvanbelle.tiemeplan.viewmodel.ScenarioValidator.validate(scenarioViewModel);
        validationBanner.setIssues(issues);
    }

    private boolean hasErrors(ScenarioViewModel vm) {
        return io.joranvanbelle.tiemeplan.viewmodel.ScenarioValidator.validate(vm).stream()
                .anyMatch(i -> i.severity() == ValidationSeverity.ERROR);
    }

    private void navigateForIssue(ValidationIssue issue) {
        if (issue == null) return;
        String code = issue.code();
        if (code == null) return;
        if (code.startsWith("CLASSROOM") || code.startsWith("TIMESLOT") || code.startsWith("TEACHER") || code.startsWith("SUBJECT") || code.startsWith("STUDENT_GROUP")) {
            navigateTo(Screen.SETUP);
        } else if (code.startsWith("COURSE")) {
            navigateTo(Screen.COURSES);
        }
    }

    private void navigateTo(Screen screen) {
        if (navigationService != null) {
            navigationService.navigateTo(screen);
        }
    }
}
