package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.CourseViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.StudentGroupViewModel;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class CoursesView implements ViewProvider {
    private final ScenarioViewModel scenarioViewModel;

    public CoursesView() {
        this(new ScenarioViewModel());
    }

    public CoursesView(ScenarioViewModel scenarioViewModel) {
        this.scenarioViewModel = scenarioViewModel;
    }

    @Override
    public String getTitle() {
        return "Courses";
    }

    @Override
    public Node getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(24));

        VBox header = new VBox(4);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Courses Editor");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Reorder with arrows or drag-and-drop. Course order affects the solver.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);

        TableView<CourseViewModel> table = new TableView<>(scenarioViewModel.courses());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<CourseViewModel, String> orderCol = new TableColumn<>("#");
        orderCol.setMaxWidth(50);
        orderCol.setMinWidth(50);
        orderCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(c.getValue()) + 1)));

        TableColumn<CourseViewModel, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(c -> c.getValue().teacherProperty());

        TableColumn<CourseViewModel, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(c -> c.getValue().subjectProperty());

        TableColumn<CourseViewModel, String> groupsCol = new TableColumn<>("Student Groups");
        groupsCol.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    StringBuilder sb = new StringBuilder();
                    for (StudentGroupViewModel g : c.getValue().studentGroups()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(g.getName());
                    }
                    return sb.toString();
                },
                c.getValue().studentGroups()));

        TableColumn<CourseViewModel, Number> countCol = new TableColumn<>("Participants");
        countCol.setCellValueFactory(c -> c.getValue().participantCountBinding());

        TableColumn<CourseViewModel, Void> reorderCol = new TableColumn<>("Reorder");
        reorderCol.setMaxWidth(120);
        reorderCol.setMinWidth(110);
        reorderCol.setCellFactory(col -> new TableCell<>() {
            private final Button upBtn = new Button("↑");
            private final Button downBtn = new Button("↓");
            private final HBox box = new HBox(4, upBtn, downBtn);

            {
                upBtn.getStyleClass().add("ghost-button");
                downBtn.getStyleClass().add("ghost-button");
                upBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx > 0) {
                        scenarioViewModel.moveCourse(idx, idx - 1);
                        table.getSelectionModel().select(idx - 1);
                    }
                });
                downBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < scenarioViewModel.courses().size() - 1) {
                        scenarioViewModel.moveCourse(idx, idx + 1);
                        table.getSelectionModel().select(idx + 1);
                    }
                });
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    int idx = getIndex();
                    upBtn.setDisable(idx <= 0);
                    downBtn.setDisable(idx < 0 || idx >= scenarioViewModel.courses().size() - 1);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(orderCol, teacherCol, subjectCol, groupsCol, countCol, reorderCol);

        table.setRowFactory(tv -> {
            TableRow<CourseViewModel> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer idx = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(idx.toString());
                    db.setContent(cc);
                    row.setOpacity(0.5);
                    event.consume();
                }
            });
            row.setOnDragDone(event -> {
                row.setOpacity(1.0);
                event.consume();
            });
            row.setOnDragOver(event -> {
                if (event.getGestureSource() != row && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    int dropIdx = row.isEmpty() ? table.getItems().size() - 1 : row.getIndex();
                    if (draggedIdx >= 0 && draggedIdx < scenarioViewModel.courses().size()) {
                        if (dropIdx >= scenarioViewModel.courses().size()) {
                            dropIdx = scenarioViewModel.courses().size() - 1;
                        }
                        if (draggedIdx != dropIdx) {
                            scenarioViewModel.moveCourse(draggedIdx, dropIdx);
                            table.getSelectionModel().select(dropIdx);
                        }
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
            return row;
        });

        VBox tableBox = new VBox(12);
        tableBox.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox actions = new HBox(8);
        Button addBtn = new Button("Add Course");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showAddDialog());
        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                int idx = table.getSelectionModel().getSelectedIndex();
                scenarioViewModel.courses().remove(sel);
                if (idx >= 0 && idx < table.getItems().size()) {
                    table.getSelectionModel().select(idx);
                } else if (!table.getItems().isEmpty()) {
                    table.getSelectionModel().selectLast();
                }
            }
        });
        Button moveUp = new Button("Move Up");
        moveUp.getStyleClass().add("secondary-button");
        moveUp.setOnAction(e -> {
            int idx = table.getSelectionModel().getSelectedIndex();
            if (idx > 0) {
                scenarioViewModel.moveCourse(idx, idx - 1);
                table.getSelectionModel().select(idx - 1);
            }
        });
        Button moveDown = new Button("Move Down");
        moveDown.getStyleClass().add("secondary-button");
        moveDown.setOnAction(e -> {
            int idx = table.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < scenarioViewModel.courses().size() - 1) {
                scenarioViewModel.moveCourse(idx, idx + 1);
                table.getSelectionModel().select(idx + 1);
            }
        });
        actions.getChildren().addAll(addBtn, moveUp, moveDown, removeBtn);
        tableBox.getChildren().add(actions);

        root.setCenter(tableBox);
        return root;
    }

    private void showAddDialog() {
        Dialog<CourseViewModel> dialog = new Dialog<>();
        dialog.setTitle("Add Course");
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        ComboBox<String> teacherCombo = new ComboBox<>(scenarioViewModel.teachers());
        teacherCombo.setPromptText("Select teacher");
        ComboBox<String> subjectCombo = new ComboBox<>(scenarioViewModel.subjects());
        subjectCombo.setPromptText("Select subject");

        Label groupsLabel = new Label("Student Groups");
        ListView<StudentGroupViewModel> groupsList = new ListView<>(scenarioViewModel.studentGroups());
        groupsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        groupsList.setCellFactory(CheckBoxListCell.forListView(param -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(groupsList.getSelectionModel().getSelectedItems().contains(param));
            prop.addListener((obs, oldv, newv) -> {
                if (newv && !groupsList.getSelectionModel().getSelectedItems().contains(param)) {
                    groupsList.getSelectionModel().select(param);
                } else if (!newv && groupsList.getSelectionModel().getSelectedItems().contains(param)) {
                    groupsList.getSelectionModel().clearSelection(groupsList.getItems().indexOf(param));
                }
            });
            return prop;
        }, new StringConverter<StudentGroupViewModel>() {
            @Override
            public String toString(StudentGroupViewModel object) {
                if (object == null) return "";
                return object.getName() + " (" + object.getSize() + ")";
            }

            @Override
            public StudentGroupViewModel fromString(String string) {
                return null;
            }
        }));

        content.getChildren().addAll(new Label("Teacher"), teacherCombo, new Label("Subject"), subjectCombo,
                groupsLabel, groupsList);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                CourseViewModel course = new CourseViewModel(
                        teacherCombo.getValue() == null ? "" : teacherCombo.getValue(),
                        subjectCombo.getValue() == null ? "" : subjectCombo.getValue());
                for (StudentGroupViewModel group : groupsList.getSelectionModel().getSelectedItems()) {
                    if (!course.studentGroups().contains(group)) {
                        course.studentGroups().add(group);
                    }
                }
                return course;
            }
            return null;
        });

        var result = dialog.showAndWait();
        result.ifPresent(course -> {
            if (!course.getTeacher().isBlank() && !course.getSubject().isBlank()
                    && !course.studentGroups().isEmpty()) {
                scenarioViewModel.courses().add(course);
            }
        });
    }
}
