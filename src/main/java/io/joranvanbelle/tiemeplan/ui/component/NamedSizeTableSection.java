package io.joranvanbelle.tiemeplan.ui.component;

import io.joranvanbelle.tiemeplan.ui.dialog.Dialogs;

import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NamedSizeTableSection<T> extends VBox {

    public NamedSizeTableSection(
            String addLabel,
            String namePrompt,
            String existingLabel,
            String sizeLabel,
            int minSize,
            int maxSize,
            int defaultSize,
            ObservableList<T> items,
            Function<T, StringProperty> nameProperty,
            Function<T, IntegerProperty> sizeProperty,
            BiFunction<String, Integer, T> factory,
            String duplicateTitle,
            String duplicateMessage) {
        setSpacing(10);
        setPadding(new Insets(8));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField nameField = new TextField();
        nameField.setPromptText(namePrompt);
        Spinner<Integer> sizeSpinner = new Spinner<>(minSize, maxSize, defaultSize);
        sizeSpinner.setEditable(true);
        sizeSpinner.setPrefWidth(120);

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label(sizeLabel), 0, 1);
        form.add(sizeSpinner, 1, 1);

        Button addBtn = new Button(addLabel);
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String value = nameField.getText();
            if (value == null || value.trim().isEmpty()) {
                return;
            }
            String trimmed = value.trim();
            boolean exists = items.stream()
                    .anyMatch(item -> nameProperty.apply(item).get() != null
                            && nameProperty.apply(item).get().equalsIgnoreCase(trimmed));
            if (exists) {
                Dialogs.warning(duplicateTitle, duplicateMessage);
                nameField.requestFocus();
                nameField.selectAll();
                return;
            }
            items.add(factory.apply(trimmed, sizeSpinner.getValue()));
            nameField.clear();
            nameField.requestFocus();
        });

        HBox formActions = new HBox(addBtn);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        TableView<T> table = new TableView<>(items);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(200);
        table.setMinHeight(160);

        TableColumn<T, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> nameProperty.apply(c.getValue()));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            var item = event.getTableView().getItems().get(event.getTablePosition().getRow());
            String newName = event.getNewValue() == null ? "" : event.getNewValue().trim();
            if (newName.isEmpty()) {
                nameProperty.apply(item).set(event.getOldValue());
                return;
            }
            boolean exists = items.stream()
                    .anyMatch(other -> other != item
                            && nameProperty.apply(other).get() != null
                            && nameProperty.apply(other).get().equalsIgnoreCase(newName));
            if (exists) {
                Dialogs.warning(duplicateTitle, duplicateMessage);
                nameProperty.apply(item).set(event.getOldValue());
                return;
            }
            nameProperty.apply(item).set(newName);
        });

        TableColumn<T, Integer> sizeCol = new TableColumn<>(sizeLabel);
        sizeCol.setCellValueFactory(c -> sizeProperty.apply(c.getValue()).asObject());
        sizeCol.setCellFactory(col -> new IntegerSpinnerTableCell<>(minSize, maxSize, defaultSize, 80, sizeProperty));
        sizeCol.setEditable(true);
        sizeCol.setMaxWidth(140);

        table.getColumns().addAll(nameCol, sizeCol);

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
            }
        });

        getChildren().addAll(new Label(addLabel), form, formActions, new Label(existingLabel), table, removeBtn);
    }
}
