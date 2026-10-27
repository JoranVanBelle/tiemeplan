package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.component.ValidationBanner;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.ValidationIssue;
import io.joranvanbelle.tiemeplan.viewmodel.ValidationSeverity;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConstraintsView implements ViewProvider {
    private final ScenarioViewModel scenarioViewModel;
    private final NavigationService navigationService;
    private final ValidationBanner validationBanner;

    public ConstraintsView() {
        this(new ScenarioViewModel(), null);
    }

    public ConstraintsView(ScenarioViewModel scenarioViewModel) {
        this(scenarioViewModel, null);
    }

    public ConstraintsView(ScenarioViewModel scenarioViewModel, NavigationService navigationService) {
        this.scenarioViewModel = scenarioViewModel;
        this.navigationService = navigationService;
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
        Label subtitleLabel = new Label("Check scenario completeness and navigate to fixes.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        VBox resourcesCard = createCard("Resources", "Classrooms, teachers, subjects, student groups");
        VBox coursesCard = createCard("Courses", "Course definitions and participant mappings");
        VBox timeslotsCard = createCard("Timeslots", "Available time slots");

        Button solveButton = new Button("Solve");
        solveButton.getStyleClass().add("primary-button");
        solveButton.setDisable(hasErrors(scenarioViewModel));
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

        root.getChildren().addAll(header, scrollPane);
        return root;
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
            solveButton.setDisable(hasErrors(scenarioViewModel));
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
        var code = issue.code();
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
