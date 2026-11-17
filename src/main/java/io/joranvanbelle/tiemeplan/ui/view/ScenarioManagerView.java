package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.model.ScenarioDto;
import io.joranvanbelle.tiemeplan.service.ScenarioPersistenceService;
import io.joranvanbelle.tiemeplan.ui.dialog.Dialogs;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ScenarioManagerView implements ViewProvider {
    private final ScenarioViewModel viewModel;
    private final ScenarioPersistenceService persistenceService;
    private final List<Button> actionButtons = new ArrayList<>();
    private VBox root;
    private Label statusLabel;
    private Task<?> activeTask;

    public ScenarioManagerView(ScenarioViewModel viewModel, ScenarioPersistenceService persistenceService) {
        this.viewModel = viewModel;
        this.persistenceService = persistenceService;
    }

    @Override
    public Node getView() {
        abortTask();
        actionButtons.clear();
        root = new VBox(24);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(32));

        VBox header = new VBox(6);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Scenario");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Import and export scenario data as JSON or YAML.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("export-status");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        VBox exportCard = new VBox(12);
        exportCard.getStyleClass().add("card");
        Label exportTitle = new Label("Export");
        exportTitle.getStyleClass().add("card-title");
        Label exportSub = new Label("Save current scenario to a file.");
        exportSub.getStyleClass().add("card-subtitle");
        HBox exportActions = new HBox(10);
        exportActions.setAlignment(Pos.CENTER_LEFT);
        Button exportJson = createButton("Export JSON", "primary-button", () -> exportScenario(true));
        Button exportYaml = createButton("Export YAML", "secondary-button", () -> exportScenario(false));
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
        Button importJson = createButton("Import JSON", "primary-button", () -> importScenario(true));
        Button importYaml = createButton("Import YAML", "secondary-button", () -> importScenario(false));
        importActions.getChildren().addAll(importJson, importYaml);
        importCard.getChildren().addAll(importTitle, importSub, importActions);

        root.getChildren().addAll(header, statusLabel, exportCard, importCard);
        return root;
    }

    private Button createButton(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> action.run());
        actionButtons.add(button);
        return button;
    }

    // todo - update schema release version to 2
    private void exportScenario(boolean json) {
        File file = chooseSaveFile(json);
        if (file == null) {
            return;
        }
        ScenarioDto dto = viewModel.toDto();
        runTask("Exporting scenario...", () -> {
            if (json) {
                persistenceService.saveJson(dto, file);
            } else {
                persistenceService.saveYaml(dto, file);
            }
            return null;
        }, ignored -> Dialogs.info(owner(), "Export successful", "Scenario saved to " + file.getName()),
                error -> Dialogs.error(owner(), "Export failed", Dialogs.messageOf(error)));
    }

    // todo - when id is missing, generate one (backwards compatible)
    private void importScenario(boolean json) {
        File file = chooseOpenFile(json);
        if (file == null) {
            return;
        }
        runTask("Importing scenario...", () -> json ? persistenceService.readJson(file)
                : persistenceService.readYaml(file), dto -> {
                    viewModel.loadFrom(dto);
                    Dialogs.info(owner(), "Import successful", "Scenario loaded from " + file.getName());
                }, error -> Dialogs.error(owner(), "Import failed", Dialogs.messageOf(error)));
    }

    private <T> void runTask(
            String message,
            Callable<T> work,
            Consumer<T> onSuccess,
            Consumer<Throwable> onFailure) {
        setBusy(true);
        statusLabel.setText(message);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        activeTask = task;
        task.setOnSucceeded(event -> {
            finishTask();
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            finishTask();
            onFailure.accept(task.getException());
        });
        task.setOnCancelled(event -> finishTask());
        Thread thread = new Thread(task, "scenario-io");
        thread.setDaemon(true);
        thread.start();
    }

    private void finishTask() {
        activeTask = null;
        setBusy(false);
        statusLabel.setText("");
    }

    private void setBusy(boolean busy) {
        for (Button button : actionButtons) {
            button.setDisable(busy);
        }
        statusLabel.setVisible(busy);
        statusLabel.setManaged(busy);
    }

    private void abortTask() {
        if (activeTask == null) {
            return;
        }
        Task<?> task = activeTask;
        activeTask = null;
        task.setOnSucceeded(null);
        task.setOnFailed(null);
        task.setOnCancelled(null);
        task.cancel();
    }

    private File chooseSaveFile(boolean json) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(json ? "Export Scenario (JSON)" : "Export Scenario (YAML)");
        chooser.setInitialFileName(json ? "tiemeplan-scenario.json" : "tiemeplan-scenario.yaml");
        chooser.getExtensionFilters().add(json
                ? new FileChooser.ExtensionFilter("JSON Files", "*.json")
                : new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"));
        File file = chooser.showSaveDialog(owner());
        if (file == null) {
            return null;
        }
        File target = ensureExtension(file, json ? ".json" : ".yaml");
        if (Files.exists(target.toPath()) && !Dialogs.confirm(owner(), "Replace file?",
                "A file with this name already exists.", target.getName() + " will be replaced.")) {
            return null;
        }
        return target;
    }

    private File chooseOpenFile(boolean json) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(json ? "Import Scenario (JSON)" : "Import Scenario (YAML)");
        chooser.getExtensionFilters().add(json
                ? new FileChooser.ExtensionFilter("JSON Files", "*.json")
                : new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"));
        return chooser.showOpenDialog(owner());
    }

    private File ensureExtension(File file, String extension) {
        if (file.getName().toLowerCase().endsWith(extension)) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + extension);
    }

    private Window owner() {
        if (root == null || root.getScene() == null) {
            return null;
        }
        return root.getScene().getWindow();
    }
}
