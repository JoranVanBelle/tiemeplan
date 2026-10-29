package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.viewmodel.ClassroomViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.StudentGroupViewModel;
import io.joranvanbelle.tiemeplan.viewmodel.TimeslotViewModel;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;

public class SetupView implements ViewProvider {
    private final ScenarioViewModel scenarioViewModel;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public SetupView() {
        this(new ScenarioViewModel());
    }

    public SetupView(ScenarioViewModel scenarioViewModel) {
        this.scenarioViewModel = scenarioViewModel;
    }

    @Override
    public String getTitle() {
        return "Setup";
    }

    @Override
    public Node getView() {
        VBox root = new VBox(16);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(24));

        VBox header = new VBox(4);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Setup / Configuration");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Define classrooms, timeslots, teachers, subjects, and student groups.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.5);

        VBox leftContent = new VBox(12);
        leftContent.setPadding(new Insets(0, 12, 0, 0));
        leftContent.getChildren().addAll(createClassroomsSection(), createTimeslotsSection());
        VBox.setVgrow(leftContent, Priority.ALWAYS);

        ScrollPane leftScroll = new ScrollPane(leftContent);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroll.getStyleClass().add("scroll-pane");

        VBox rightContent = new VBox(12);
        rightContent.setPadding(new Insets(0, 0, 0, 12));
        rightContent.getChildren().addAll(createTeachersSection(), createSubjectsSection(),
                createStudentGroupsSection());
        VBox.setVgrow(rightContent, Priority.ALWAYS);

        ScrollPane rightScroll = new ScrollPane(rightContent);
        rightScroll.setFitToWidth(true);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.getStyleClass().add("scroll-pane");

        split.getItems().addAll(leftScroll, rightScroll);
        VBox.setVgrow(split, Priority.ALWAYS);

        root.getChildren().addAll(header, split);
        return root;
    }

    private TitledPane createClassroomsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(8));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Room A101");
        Spinner<Integer> capSpinner = new Spinner<>(1, 1000, 30);
        capSpinner.setEditable(true);
        capSpinner.setPrefWidth(120);

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Capacity"), 0, 1);
        form.add(capSpinner, 1, 1);

        Button addBtn = new Button("Add Classroom");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                return;
            }
            String trimmed = name.trim();
            boolean exists = scenarioViewModel.classrooms().stream()
                    .anyMatch(c -> c.getName() != null && c.getName().equalsIgnoreCase(trimmed));
            if (exists) {
                showWarning("Duplicate Classroom", "A classroom with this name already exists.");
                nameField.requestFocus();
                nameField.selectAll();
                return;
            }
            scenarioViewModel.classrooms().add(new ClassroomViewModel(trimmed, capSpinner.getValue()));
            nameField.clear();
            nameField.requestFocus();
        });

        HBox formActions = new HBox(addBtn);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        TableView<ClassroomViewModel> table = new TableView<>(scenarioViewModel.classrooms());
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(200);
        table.setMinHeight(160);

        TableColumn<ClassroomViewModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            var item = e.getTableView().getItems().get(e.getTablePosition().getRow());
            String newName = e.getNewValue() == null ? "" : e.getNewValue().trim();
            if (newName.isEmpty()) {
                item.setName(e.getOldValue());
                return;
            }
            boolean exists = scenarioViewModel.classrooms().stream()
                    .anyMatch(c -> c != item && c.getName() != null && c.getName().equalsIgnoreCase(newName));
            if (exists) {
                showWarning("Duplicate Classroom", "A classroom with this name already exists.");
                item.setName(e.getOldValue());
                return;
            }
            item.setName(newName);
        });

        TableColumn<ClassroomViewModel, Integer> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(c -> c.getValue().capacityProperty().asObject());
        capCol.setCellFactory(col -> new javafx.scene.control.TableCell<ClassroomViewModel, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 30));
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(80);
                spinner.valueProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null && getTableRow() != null && getTableRow().getItem() != null && isEditing()) {
                        getTableRow().getItem().setCapacity(newV);
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                if (isEditing()) {
                    setText(null);
                    spinner.getValueFactory().setValue(getTableRow().getItem().getCapacity());
                    setGraphic(spinner);
                } else {
                    setGraphic(null);
                    setText(String.valueOf(getTableRow().getItem().getCapacity()));
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                updateItem(getItem(), isEmpty());
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateItem(getItem(), isEmpty());
            }
        });
        capCol.setEditable(true);
        capCol.setMaxWidth(140);

        table.getColumns().addAll(nameCol, capCol);

        table.setRowFactory(tv -> {
            TableRow<ClassroomViewModel> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 1) {
                        tv.getSelectionModel().select(row.getIndex());
                    } else if (event.getClickCount() == 2 && !row.isEmpty()) {
                        String target = event.getPickResult().getIntersectedNode() == null ? ""
                                : event.getPickResult().getIntersectedNode().toString();
                        if (target.contains("Capacity") || target.contains("Spinner")) {
                            tv.edit(row.getIndex(), capCol);
                        } else {
                            tv.edit(row.getIndex(), nameCol);
                        }
                    }
                    event.consume();
                }
            });
            return row;
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                scenarioViewModel.classrooms().remove(sel);
            }
        });

        box.getChildren().addAll(new Label("Add Classroom"), form, formActions, new Label("Existing Classrooms"), table,
                removeBtn);
        TitledPane pane = new TitledPane("Classrooms", box);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane createTimeslotsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(8));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        ComboBox<DayOfWeek> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll(DayOfWeek.values());
        dayCombo.setValue(DayOfWeek.MONDAY);
        dayCombo.setMaxWidth(Double.MAX_VALUE);

        TextField timeField = new TextField("09:00");
        timeField.setPromptText("HH:mm");

        form.add(new Label("Day"), 0, 0);
        form.add(dayCombo, 1, 0);
        form.add(new Label("Start Time"), 0, 1);
        form.add(timeField, 1, 1);

        Button addBtn = new Button("Add Timeslot");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            try {
                LocalTime time = LocalTime.parse(timeField.getText().trim(), TIME_FMT);
                DayOfWeek day = dayCombo.getValue();
                if (day == null) {
                    return;
                }
                boolean exists = scenarioViewModel.timeslots().stream()
                        .anyMatch(t -> t.getDayOfWeek() == day && time.equals(t.getStartTime()));
                if (exists) {
                    showWarning("Duplicate Timeslot", "This timeslot already exists.");
                    timeField.requestFocus();
                    timeField.selectAll();
                    return;
                }
                scenarioViewModel.timeslots().add(new TimeslotViewModel(day, time));
            } catch (DateTimeParseException ex) {
                timeField.requestFocus();
                timeField.selectAll();
            }
        });

        HBox formActions = new HBox(addBtn);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        TableView<TimeslotViewModel> table = new TableView<>(scenarioViewModel.timeslots());
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(200);
        table.setMinHeight(160);

        TableColumn<TimeslotViewModel, DayOfWeek> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> c.getValue().dayOfWeekProperty());
        dayCol.setCellFactory(ComboBoxTableCell.forTableColumn(DayOfWeek.values()));
        dayCol.setOnEditCommit(
                e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setDayOfWeek(e.getNewValue()));

        TableColumn<TimeslotViewModel, LocalTime> timeCol = new TableColumn<>("Start Time");
        timeCol.setCellValueFactory(c -> c.getValue().startTimeProperty());
        timeCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalTimeStringConverter()));
        timeCol.setOnEditCommit(
                e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setStartTime(e.getNewValue()));
        timeCol.setMaxWidth(140);

        table.getColumns().addAll(dayCol, timeCol);

        table.setRowFactory(tv -> {
            TableRow<TimeslotViewModel> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 1) {
                        tv.getSelectionModel().select(row.getIndex());
                    } else if (event.getClickCount() == 2 && !row.isEmpty()) {
                        String target = event.getPickResult().getIntersectedNode() == null ? ""
                                : event.getPickResult().getIntersectedNode().toString();
                        if (target.contains("ComboBox")) {
                            tv.edit(row.getIndex(), dayCol);
                        } else {
                            tv.edit(row.getIndex(), timeCol);
                        }
                    }
                    event.consume();
                }
            });
            return row;
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                scenarioViewModel.timeslots().remove(sel);
            }
        });

        box.getChildren().addAll(new Label("Add Timeslot"), form, formActions, new Label("Existing Timeslots"), table,
                removeBtn);
        TitledPane pane = new TitledPane("Timeslots", box);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane createTeachersSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(8));

        HBox form = new HBox(8);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField field = new TextField();
        field.setPromptText("Teacher name");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String v = field.getText();
            if (v != null && !v.trim().isEmpty()) {
                String trimmed = v.trim();
                boolean exists = scenarioViewModel.teachers().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(trimmed));
                if (exists) {
                    showWarning("Duplicate Teacher", "A teacher with this name already exists.");
                    field.requestFocus();
                    field.selectAll();
                    return;
                }
                scenarioViewModel.teachers().add(trimmed);
                field.clear();
                field.requestFocus();
            }
        });
        form.getChildren().addAll(field, addBtn);

        ListView<String> list = new ListView<>(scenarioViewModel.teachers());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setEditable(true);
        list.setCellFactory(TextFieldListCell.forListView());
        list.setPrefHeight(160);
        list.setMinHeight(140);

        list.setOnEditCommit(event -> {
            String oldName = event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.teachers().size()
                    ? scenarioViewModel.teachers().get(event.getIndex())
                    : event.getNewValue();
            String newName = event.getNewValue() == null ? "" : event.getNewValue().trim();
            if (newName.isEmpty()) {
                if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.teachers().size()) {
                    list.getItems().set(event.getIndex(), oldName);
                }
                return;
            }
            boolean exists = scenarioViewModel.teachers().stream()
                    .anyMatch(t -> !t.equals(oldName) && t.equalsIgnoreCase(newName));
            if (exists) {
                showWarning("Duplicate Teacher", "A teacher with this name already exists.");
                if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.teachers().size()) {
                    list.getItems().set(event.getIndex(), oldName);
                }
                return;
            }
            if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.teachers().size()) {
                scenarioViewModel.teachers().set(event.getIndex(), newName);
            }
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            String sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                scenarioViewModel.teachers().remove(sel);
            }
        });

        box.getChildren().addAll(new Label("Add Teacher"), form, new Label("Teachers"), list, removeBtn);
        TitledPane pane = new TitledPane("Teachers", box);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane createSubjectsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(8));

        HBox form = new HBox(8);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField field = new TextField();
        field.setPromptText("Subject name");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String v = field.getText();
            if (v != null && !v.trim().isEmpty()) {
                String trimmed = v.trim();
                boolean exists = scenarioViewModel.subjects().stream()
                        .anyMatch(s -> s.equalsIgnoreCase(trimmed));
                if (exists) {
                    showWarning("Duplicate Subject", "A subject with this name already exists.");
                    field.requestFocus();
                    field.selectAll();
                    return;
                }
                scenarioViewModel.subjects().add(trimmed);
                field.clear();
                field.requestFocus();
            }
        });
        form.getChildren().addAll(field, addBtn);

        ListView<String> list = new ListView<>(scenarioViewModel.subjects());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setEditable(true);
        list.setCellFactory(TextFieldListCell.forListView());
        list.setPrefHeight(160);
        list.setMinHeight(140);

        list.setOnEditCommit(event -> {
            String oldName = event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.subjects().size()
                    ? scenarioViewModel.subjects().get(event.getIndex())
                    : event.getNewValue();
            String newName = event.getNewValue() == null ? "" : event.getNewValue().trim();
            if (newName.isEmpty()) {
                if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.subjects().size()) {
                    list.getItems().set(event.getIndex(), oldName);
                }
                return;
            }
            boolean exists = scenarioViewModel.subjects().stream()
                    .anyMatch(s -> !s.equals(oldName) && s.equalsIgnoreCase(newName));
            if (exists) {
                showWarning("Duplicate Subject", "A subject with this name already exists.");
                if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.subjects().size()) {
                    list.getItems().set(event.getIndex(), oldName);
                }
                return;
            }
            if (event.getIndex() >= 0 && event.getIndex() < scenarioViewModel.subjects().size()) {
                scenarioViewModel.subjects().set(event.getIndex(), newName);
            }
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            String sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                scenarioViewModel.subjects().remove(sel);
            }
        });

        box.getChildren().addAll(new Label("Add Subject"), form, new Label("Subjects"), list, removeBtn);
        TitledPane pane = new TitledPane("Subjects", box);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane createStudentGroupsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(8));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Group 1A");
        Spinner<Integer> sizeSpinner = new Spinner<>(1, 500, 20);
        sizeSpinner.setEditable(true);
        sizeSpinner.setPrefWidth(120);

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Size"), 0, 1);
        form.add(sizeSpinner, 1, 1);

        Button addBtn = new Button("Add Student Group");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                return;
            }
            String trimmed = name.trim();
            boolean exists = scenarioViewModel.studentGroups().stream()
                    .anyMatch(g -> g.getName() != null && g.getName().equalsIgnoreCase(trimmed));
            if (exists) {
                showWarning("Duplicate Student Group", "A student group with this name already exists.");
                nameField.requestFocus();
                nameField.selectAll();
                return;
            }
            scenarioViewModel.studentGroups().add(new StudentGroupViewModel(trimmed, sizeSpinner.getValue()));
            nameField.clear();
            nameField.requestFocus();
        });

        HBox formActions = new HBox(addBtn);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        TableView<StudentGroupViewModel> table = new TableView<>(scenarioViewModel.studentGroups());
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(200);
        table.setMinHeight(160);

        TableColumn<StudentGroupViewModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            var item = e.getTableView().getItems().get(e.getTablePosition().getRow());
            String newName = e.getNewValue() == null ? "" : e.getNewValue().trim();
            if (newName.isEmpty()) {
                item.setName(e.getOldValue());
                return;
            }
            boolean exists = scenarioViewModel.studentGroups().stream()
                    .anyMatch(g -> g != item && g.getName() != null && g.getName().equalsIgnoreCase(newName));
            if (exists) {
                showWarning("Duplicate Student Group", "A student group with this name already exists.");
                item.setName(e.getOldValue());
                return;
            }
            item.setName(newName);
        });

        TableColumn<StudentGroupViewModel, Integer> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(c -> c.getValue().sizeProperty().asObject());
        sizeCol.setCellFactory(col -> new javafx.scene.control.TableCell<StudentGroupViewModel, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 20));
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(80);
                spinner.valueProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null && getTableRow() != null && getTableRow().getItem() != null && isEditing()) {
                        getTableRow().getItem().setSize(newV);
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                if (isEditing()) {
                    setText(null);
                    spinner.getValueFactory().setValue(getTableRow().getItem().getSize());
                    setGraphic(spinner);
                } else {
                    setGraphic(null);
                    setText(String.valueOf(getTableRow().getItem().getSize()));
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                updateItem(getItem(), isEmpty());
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateItem(getItem(), isEmpty());
            }
        });
        sizeCol.setEditable(true);
        sizeCol.setMaxWidth(120);

        table.getColumns().addAll(nameCol, sizeCol);

        table.setRowFactory(tv -> {
            TableRow<StudentGroupViewModel> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 1) {
                        tv.getSelectionModel().select(row.getIndex());
                    } else if (event.getClickCount() == 2 && !row.isEmpty()) {
                        String target = event.getPickResult().getIntersectedNode() == null ? ""
                                : event.getPickResult().getIntersectedNode().toString();
                        if (target.contains("Size")) {
                            tv.edit(row.getIndex(), sizeCol);
                        } else {
                            tv.edit(row.getIndex(), nameCol);
                        }
                    }
                    event.consume();
                }
            });
            return row;
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                scenarioViewModel.studentGroups().remove(sel);
            }
        });

        box.getChildren().addAll(new Label("Add Student Group"), form, formActions,
                new Label("Existing Student Groups"), table, removeBtn);
        TitledPane pane = new TitledPane("Student Groups", box);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
