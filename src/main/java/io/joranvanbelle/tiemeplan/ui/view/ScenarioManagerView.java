package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.service.ScenarioPersistenceService;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ScenarioManagerView implements ViewProvider {
    private final ScenarioViewModel viewModel = AppState.getInstance().getScenarioViewModel();
    private final ScenarioPersistenceService persistenceService = new ScenarioPersistenceService();

    @Override
    public String getTitle() {
        return "Scenario";
    }

    @Override
    public Node getView() {
        VBox root = new VBox(24);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(32));

        VBox header = new VBox(6);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Scenario");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Import and export scenario data as JSON or YAML.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        VBox exportCard = new VBox(12);
        exportCard.getStyleClass().add("card");
        Label exportTitle = new Label("Export");
        exportTitle.getStyleClass().add("card-title");
        Label exportSub = new Label("Save current scenario to a file.");
        exportSub.getStyleClass().add("card-subtitle");
        HBox exportActions = new HBox(10);
        exportActions.setAlignment(Pos.CENTER_LEFT);
        Button exportJson = new Button("Export JSON");
        exportJson.getStyleClass().add("primary-button");
        exportJson.setOnAction(e -> exportJson());
        Button exportYaml = new Button("Export YAML");
        exportYaml.getStyleClass().add("secondary-button");
        exportYaml.setOnAction(e -> exportYaml());
        exportActions.getChildren().addAll(exportJson, exportYaml);
        exportCard.getChildren().addAll(exportTitle, exportSub, exportActions);

        VBox importCard = new VBox(12);
        importCard.getStyleClass().add("card");
        Label importTitle = new Label("Import");
        importTitle.getStyleClass().add("card-title");
        Label importSub = new Label("Load a scenario from JSON or YAML.");
        importSub.getStyleClass().add("card-subtitle");
        HBox importActions = new HBox(10);
        importActions.setAlignment(Pos.CENTER_LEFT);
        Button importJson = new Button("Import JSON");
        importJson.getStyleClass().add("primary-button");
        importJson.setOnAction(e -> importJson());
        Button importYaml = new Button("Import YAML");
        importYaml.getStyleClass().add("secondary-button");
        importYaml.setOnAction(e -> importYaml());
        importActions.getChildren().addAll(importJson, importYaml);
        importCard.getChildren().addAll(importTitle, importSub, importActions);

        root.getChildren().addAll(header, exportCard, importCard);
        return root;
    }

    private void exportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Scenario (JSON)");
        chooser.setInitialFileName("tiemeplan-scenario.json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(rootStage());
        if (file == null) {
            return;
        }
        try {
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }
            persistenceService.saveJson(viewModel, file);
            showInfo("Export successful", "Scenario saved to " + file.getName());
        } catch (Exception ex) {
            showError("Export failed", ex.getMessage());
        }
    }

    private void exportYaml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Scenario (YAML)");
        chooser.setInitialFileName("tiemeplan-scenario.yaml");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"));
        File file = chooser.showSaveDialog(rootStage());
        if (file == null) {
            return;
        }
        try {
            if (!file.getName().toLowerCase().endsWith(".yaml") && !file.getName().toLowerCase().endsWith(".yml")) {
                file = new File(file.getParentFile(), file.getName() + ".yaml");
            }
            persistenceService.saveYaml(viewModel, file);
            showInfo("Export successful", "Scenario saved to " + file.getName());
        } catch (Exception ex) {
            showError("Export failed", ex.getMessage());
        }
    }

    private void importJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Scenario (JSON)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(rootStage());
        if (file == null) {
            return;
        }
        try {
            persistenceService.loadJson(viewModel, file);
            showInfo("Import successful", "Scenario loaded from " + file.getName());
        } catch (Exception ex) {
            showError("Import failed", ex.getMessage());
        }
    }

    private void importYaml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Scenario (YAML)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"));
        File file = chooser.showOpenDialog(rootStage());
        if (file == null) {
            return;
        }
        try {
            persistenceService.loadYaml(viewModel, file);
            showInfo("Import successful", "Scenario loaded from " + file.getName());
        } catch (Exception ex) {
            showError("Import failed", ex.getMessage());
        }
    }

    private javafx.stage.Stage rootStage() {
        return javafx.stage.Window.getWindows().stream()
                .filter(w -> w.isShowing())
                .findFirst()
                .map(javafx.stage.Window::getScene)
                .map(s -> (javafx.stage.Stage) s.getWindow())
                .orElse(null);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Unknown error" : message);
        alert.showAndWait();
    }
}
