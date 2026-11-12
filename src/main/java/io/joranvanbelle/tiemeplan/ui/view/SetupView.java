package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.component.NamedSizeTableSection;
import io.joranvanbelle.tiemeplan.ui.component.StringListSection;
import io.joranvanbelle.tiemeplan.ui.dialog.Dialogs;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class SetupView implements ViewProvider {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final StringConverter<LocalTime> TIME_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(LocalTime value) {
            return value == null ? "" : TIME_FMT.format(value);
        }

        @Override
        public LocalTime fromString(String text) {
            if (text == null || text.isBlank()) {
                return null;
            }
            try {
                return LocalTime.parse(text.trim(), TIME_FMT);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    };

    private final ScenarioViewModel scenarioViewModel;

    public SetupView() {
        this(new ScenarioViewModel());
    }

    public SetupView(ScenarioViewModel scenarioViewModel) {
        this.scenarioViewModel = scenarioViewModel;
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

        ScrollPane leftScroll = createScrollPane(leftContent);

        VBox rightContent = new VBox(12);
        rightContent.setPadding(new Insets(0, 0, 0, 12));
        rightContent.getChildren().addAll(createTeachersSection(), createSubjectsSection(),
                createStudentGroupsSection());
        VBox.setVgrow(rightContent, Priority.ALWAYS);

        ScrollPane rightScroll = createScrollPane(rightContent);

        split.getItems().addAll(leftScroll, rightScroll);
        VBox.setVgrow(split, Priority.ALWAYS);

        root.getChildren().addAll(header, split);
        return root;
    }

    private ScrollPane createScrollPane(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("scroll-pane");
        return scroll;
    }

    private TitledPane section(String title, Node content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane createClassroomsSection() {
        return section("Classrooms", new NamedSizeTableSection<>(
                "Add Classroom", "e.g. Room A101", "Existing Classrooms", "Capacity",
                1, 1000, 30, scenarioViewModel.classrooms(),
                ClassroomViewModel::nameProperty, ClassroomViewModel::capacityProperty,
                ClassroomViewModel::new, "Duplicate Classroom", "A classroom with this name already exists."));
    }

    private TitledPane createStudentGroupsSection() {
        return section("Student Groups", new NamedSizeTableSection<>(
                "Add Student Group", "e.g. Group 1A", "Existing Student Groups", "Size",
                1, 500, 20, scenarioViewModel.studentGroups(),
                StudentGroupViewModel::nameProperty, StudentGroupViewModel::sizeProperty,
                StudentGroupViewModel::new, "Duplicate Student Group",
                "A student group with this name already exists."));
    }

    private TitledPane createTeachersSection() {
        return section("Teachers", new StringListSection(
                "Add Teacher", "Teacher name", "Teachers", scenarioViewModel.teachers(),
                "Duplicate Teacher", "A teacher with this name already exists."));
    }

    private TitledPane createSubjectsSection() {
        return section("Subjects", new StringListSection(
                "Add Subject", "Subject name", "Subjects", scenarioViewModel.subjects(),
                "Duplicate Subject", "A subject with this name already exists."));
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
                    Dialogs.warning("Duplicate Timeslot", "This timeslot already exists.");
                    timeField.requestFocus();
                    timeField.selectAll();
                    return;
                }
                scenarioViewModel.timeslots().add(new TimeslotViewModel(day, time));
            } catch (DateTimeParseException ex) {
                Dialogs.warning("Invalid time", "Enter a time in HH:mm format.");
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
        timeCol.setCellFactory(TextFieldTableCell.forTableColumn(TIME_CONVERTER));
        timeCol.setOnEditCommit(e -> {
            TimeslotViewModel item = e.getTableView().getItems().get(e.getTablePosition().getRow());
            LocalTime newTime = e.getNewValue();
            if (newTime == null) {
                Dialogs.warning("Invalid time", "Enter a time in HH:mm format, e.g. 09:00.");
                return;
            }
            item.setStartTime(newTime);
        });
        timeCol.setMaxWidth(140);

        table.getColumns().addAll(dayCol, timeCol);

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("ghost-button");
        removeBtn.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                scenarioViewModel.timeslots().remove(selected);
            }
        });

        box.getChildren().addAll(new Label("Add Timeslot"), form, formActions, new Label("Existing Timeslots"), table,
                removeBtn);
        return section("Timeslots", box);
    }
}
