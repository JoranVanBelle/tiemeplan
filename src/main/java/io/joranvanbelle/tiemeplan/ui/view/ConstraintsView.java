package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.service.SolverService;
import io.joranvanbelle.tiemeplan.ui.component.ValidationBanner;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ClassroomViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.TimeslotViewModel;
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
    private Button solveButton;
    private boolean listenersRegistered;

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
        updateValidation();
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
        VBox blockedCard = createBlockedClassroomsCard();

        Button solveButton = new Button("Solve");
        this.solveButton = solveButton;
        solveButton.getStyleClass().add("primary-button");
        solveButton.setDisable(hasErrors(scenarioViewModel));
        solveButton.setOnAction(e -> startSolve(solveButton));
        addListeners();

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
        content.getChildren().addAll(resourcesCard, coursesCard, timeslotsCard, blockedCard, validationBanner, actions);

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
        cancel.setOnAction(_ -> solverService.requestCancel());
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
        solverService.setOnRunning(_ -> {
        });
        solverService.setOnSucceeded(_ -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            BacktrackResult result = solverService.getValue();
            AppState.getInstance().setLastResult(result);
            if (navigationService != null) {
                navigationService.navigateTo(Screen.TIMETABLE);
            }
        });
        solverService.setOnFailed(_ -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            AppState.getInstance().setLastResult(
                    new BacktrackResult.NoSolutionFound(io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.empty()));
            if (navigationService != null) {
                navigationService.navigateTo(Screen.TIMETABLE);
            }
        });
        solverService.setOnCancelled(_ -> {
            overlay.setVisible(false);
            solveButton.setDisable(hasErrors(scenarioViewModel));
            AppState.getInstance().setLastResult(
                    new BacktrackResult.Cancelled(io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.empty()));
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

    private VBox createBlockedClassroomsCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        Label title = new Label("Blocked Classrooms");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Mark specific timeslots as unavailable for a classroom.");
        subtitle.getStyleClass().add("card-subtitle");

        javafx.scene.control.ComboBox<ClassroomViewModel> roomCombo = new javafx.scene.control.ComboBox<>();
        roomCombo.setItems(scenarioViewModel.classrooms());
        roomCombo.setPromptText("Select classroom");
        roomCombo.setPrefWidth(220);
        roomCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ClassroomViewModel object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public ClassroomViewModel fromString(String string) {
                return null;
            }
        });

        javafx.scene.control.ComboBox<TimeslotViewModel> slotCombo = new javafx.scene.control.ComboBox<>();
        slotCombo.setItems(scenarioViewModel.timeslots());
        slotCombo.setPromptText("Select timeslot");
        slotCombo.setPrefWidth(220);
        slotCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TimeslotViewModel object) {
                return object == null ? "" : object.getDayOfWeek() + " " + object.getStartTime();
            }

            @Override
            public TimeslotViewModel fromString(String string) {
                return null;
            }
        });

        javafx.scene.control.Button addBlock = new javafx.scene.control.Button("Block Timeslot");
        addBlock.getStyleClass().add("secondary-button");
        addBlock.setOnAction(e -> {
            ClassroomViewModel room = roomCombo.getValue();
            TimeslotViewModel slot = slotCombo.getValue();
            if (room == null || slot == null) {
                return;
            }
            boolean exists = room.occupiedTimeslots().stream()
                    .anyMatch(ts -> ts.getDayOfWeek() == slot.getDayOfWeek()
                            && ts.getStartTime().equals(slot.getStartTime()));
            if (!exists) {
                room.occupiedTimeslots().add(new TimeslotViewModel(slot.getDayOfWeek(), slot.getStartTime()));
            }
        });

        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        form.getChildren().addAll(roomCombo, slotCombo, addBlock);

        javafx.scene.control.ListView<TimeslotViewModel> blockedList = new javafx.scene.control.ListView<>();
        blockedList.setPrefHeight(120);
        blockedList.setCellFactory(_ -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TimeslotViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDayOfWeek() + " " + item.getStartTime());
                }
            }
        });

        roomCombo.valueProperty().addListener((_, _, newv) -> {
            blockedList.setItems(newv == null ? null : newv.occupiedTimeslots());
        });
        if (roomCombo.getValue() != null) {
            blockedList.setItems(roomCombo.getValue().occupiedTimeslots());
        }

        javafx.scene.control.Button removeBlock = new javafx.scene.control.Button("Remove Selected");
        removeBlock.getStyleClass().add("ghost-button");
        removeBlock.setOnAction(e -> {
            ClassroomViewModel room = roomCombo.getValue();
            TimeslotViewModel selected = blockedList.getSelectionModel().getSelectedItem();
            if (room == null || selected == null) {
                return;
            }
            room.occupiedTimeslots().remove(selected);
        });

        card.getChildren().addAll(title, subtitle, form, new Label("Blocked for selected classroom"), blockedList,
                removeBlock);
        return card;
    }

    private void addListeners() {
        if (listenersRegistered) {
            return;
        }
        listenersRegistered = true;
        javafx.beans.InvalidationListener l = _ -> {
            updateValidation();
            if (solveButton != null) {
                solveButton.setDisable(solverService.getState() == Worker.State.RUNNING
                        || hasErrors(scenarioViewModel));
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
        List<ValidationIssue> issues = io.joranvanbelle.tiemeplan.viewmodel.ScenarioValidator
                .validate(scenarioViewModel);
        validationBanner.setIssues(issues);
    }

    private boolean hasErrors(ScenarioViewModel vm) {
        return io.joranvanbelle.tiemeplan.viewmodel.ScenarioValidator.validate(vm).stream()
                .anyMatch(i -> i.severity() == ValidationSeverity.ERROR);
    }

    private void navigateForIssue(ValidationIssue issue) {
        if (issue == null)
            return;
        String code = issue.code();
        if (code == null)
            return;
        if (code.startsWith("CLASSROOM") || code.startsWith("TIMESLOT") || code.startsWith("TEACHER")
                || code.startsWith("SUBJECT") || code.startsWith("STUDENT_GROUP")) {
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
