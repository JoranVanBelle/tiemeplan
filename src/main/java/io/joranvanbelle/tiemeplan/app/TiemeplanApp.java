package io.joranvanbelle.tiemeplan.app;

import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.service.ThemeService;
import io.joranvanbelle.tiemeplan.service.export.ExportTheme;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class TiemeplanApp extends Application {

    private final ThemeService themeService = new ThemeService();
    private NavigationService navigationService;
    private final ScenarioViewModel scenarioViewModel = AppState.getInstance().getScenarioViewModel();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        BorderPane contentPane = new BorderPane();
        contentPane.getStyleClass().add("content-pane");
        navigationService = new NavigationService(contentPane, scenarioViewModel,
                AppState.getInstance()::getLastResult,
                () -> themeService.getCurrentTheme() == ThemeService.Theme.DARK ? ExportTheme.DARK : ExportTheme.LIGHT);

        root.setCenter(contentPane);

        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("toolbar");
        toolBar.setPadding(new Insets(6, 12, 6, 12));

        Label appTitle = new Label("Tiemeplan");
        appTitle.getStyleClass().add("app-title");

        Button setupBtn = new Button("Setup");
        Button coursesBtn = new Button("Courses");
        Button constraintsBtn = new Button("Constraints");
        Button timetableBtn = new Button("Timetable");
        Button scenarioBtn = new Button("Scenario");
        Button aboutBtn = new Button("About");

        setupBtn.getStyleClass().add("nav-button");
        coursesBtn.getStyleClass().add("nav-button");
        constraintsBtn.getStyleClass().add("nav-button");
        timetableBtn.getStyleClass().add("nav-button");
        scenarioBtn.getStyleClass().add("nav-button");
        aboutBtn.getStyleClass().add("nav-button");

        setupBtn.setOnAction(e -> {
            setActive(setupBtn);
            navigationService.navigateTo(Screen.SETUP);
        });
        coursesBtn.setOnAction(e -> {
            setActive(coursesBtn);
            navigationService.navigateTo(Screen.COURSES);
        });
        constraintsBtn.setOnAction(e -> {
            setActive(constraintsBtn);
            navigationService.navigateTo(Screen.CONSTRAINTS);
        });
        timetableBtn.setOnAction(e -> {
            setActive(timetableBtn);
            navigationService.navigateTo(Screen.TIMETABLE);
        });
        scenarioBtn.setOnAction(e -> {
            setActive(scenarioBtn);
            navigationService.navigateTo(Screen.SCENARIO);
        });
        aboutBtn.setOnAction(e -> {
            setActive(aboutBtn);
            navigationService.navigateTo(Screen.ABOUT);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeToggle = new Button("Toggle Theme");
        themeToggle.getStyleClass().addAll("nav-button", "theme-toggle");

        toolBar.getItems().addAll(appTitle, setupBtn, coursesBtn, constraintsBtn, timetableBtn, scenarioBtn, aboutBtn,
                spacer, themeToggle);
        root.setTop(toolBar);

        Scene scene = new Scene(root, 1200, 800);
        themeService.initialize(scene);
        themeToggle.setOnAction(e -> themeService.toggleTheme());

        stage.setTitle("Tiemeplan");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();

        setActive(setupBtn);
        navigationService.navigateTo(Screen.SETUP);
    }

    private void setActive(Button active) {
        for (var item : ((ToolBar) ((BorderPane) active.getScene().getRoot()).getTop()).getItems()) {
            if (item instanceof Button btn && btn.getStyleClass().contains("nav-button")
                    && !btn.getStyleClass().contains("theme-toggle")) {
                btn.getStyleClass().remove("active");
            }
        }
        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }
}
