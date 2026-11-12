package io.joranvanbelle.tiemeplan.ui.component;

import io.joranvanbelle.tiemeplan.ui.dialog.Dialogs;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StringListSection extends VBox {

    public StringListSection(
            String addLabel,
            String prompt,
            String existingLabel,
            ObservableList<String> items,
            String duplicateTitle,
            String duplicateMessage) {
        setSpacing(10);
        setPadding(new Insets(8));

        HBox form = new HBox(8);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField field = new TextField();
        field.setPromptText(prompt);
        HBox.setHgrow(field, Priority.ALWAYS);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String value = field.getText();
            if (value == null || value.trim().isEmpty()) {
                return;
            }
            String trimmed = value.trim();
            boolean exists = items.stream().anyMatch(item -> item.equalsIgnoreCase(trimmed));
            if (exists) {
                Dialogs.warning(duplicateTitle, duplicateMessage);
                field.requestFocus();
                field.selectAll();
                return;
            }
            items.add(trimmed);
            field.clear();
            field.requestFocus();
        });
        form.getChildren().addAll(field, addBtn);

        ListView<String> list = new ListView<>(items);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setEditable(true);
        list.setCellFactory(TextFieldListCell.forListView());
        list.setPrefHeight(160);
        list.setMinHeight(140);

        list.setOnEditCommit(event -> {
            int index = event.getIndex();
            if (index < 0 || index >= items.size()) {
                return;
            }
            String oldName = items.get(index);
            String newName = event.getNewValue() == null ? "" : event.getNewValue().trim();
            if (newName.isEmpty()) {
                list.getItems().set(index, oldName);
                return;
            }
            boolean exists = items.stream()
                    .anyMatch(item -> !item.equals(oldName) && item.equalsIgnoreCase(newName));
            if (exists) {
                Dialogs.warning(duplicateTitle, duplicateMessage);
                list.getItems().set(index, oldName);
                return;
            }
            items.set(index, newName);
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            String selected = list.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
            }
        });

        getChildren().addAll(new Label(addLabel), form, new Label(existingLabel), list, removeBtn);
    }
}
