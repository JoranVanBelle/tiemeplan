package io.joranvanbelle.tiemeplan.app;

import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.service.ScenarioPersistenceService;
import io.joranvanbelle.tiemeplan.service.ThemeService;
import io.joranvanbelle.tiemeplan.service.export.ExportService;
import io.joranvanbelle.tiemeplan.service.export.ExportTheme;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
        navigationService = new NavigationService(contentPane);
        ScreenFactory screenFactory = new ScreenFactory(navigationService, scenarioViewModel,
                AppState.getInstance()::getLastResult, AppState.getInstance()::setLastResult,
                AppState.getInstance().getSolverService(), new ExportService(), new ScenarioPersistenceService(),
                () -> themeService.getCurrentTheme() == ThemeService.Theme.DARK ? ExportTheme.DARK
                        : ExportTheme.LIGHT,
                getHostServices()::showDocument);
        screenFactory.createProviders().forEach(navigationService::register);

        root.setCenter(contentPane);

        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("toolbar");
        toolBar.setPadding(new Insets(6, 12, 6, 12));

        Label appTitle = new Label("Tiemeplan");
        appTitle.getStyleClass().add("app-title");
        toolBar.getItems().add(appTitle);

        ToggleGroup navigationGroup = new ToggleGroup();
        ToggleButton defaultButton = null;
        for (Screen screen : Screen.values()) {
            ToggleButton button = new ToggleButton(screen.getTitle());
            button.getStyleClass().add("nav-button");
            button.setToggleGroup(navigationGroup);
            button.setAccessibleText("Open " + screen.getTitle());
            button.setOnAction(event -> {
                button.setSelected(true);
                navigationService.navigateTo(screen);
            });
            toolBar.getItems().add(button);
            if (screen == Screen.SETUP) {
                defaultButton = button;
            }
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeToggle = new Button("Toggle Theme");
        themeToggle.getStyleClass().addAll("nav-button", "theme-toggle");

        toolBar.getItems().addAll(spacer, themeToggle);
        root.setTop(toolBar);

        Scene scene = new Scene(root, 1200, 800);
        themeService.initialize(scene);
        themeToggle.setOnAction(e -> themeService.toggleTheme());

        stage.setTitle("Tiemeplan");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();

        if (defaultButton != null) {
            defaultButton.setSelected(true);
        }
        navigationService.navigateTo(Screen.SETUP);
    }
}
