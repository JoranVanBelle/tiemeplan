package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.backtrack.ScheduledCourse;
import io.joranvanbelle.tiemeplan.service.AppState;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TimetableView implements ViewProvider {
    private final io.joranvanbelle.tiemeplan.ui.navigation.NavigationService navigationService;

    public TimetableView() {
        this(null);
    }

    public TimetableView(io.joranvanbelle.tiemeplan.ui.navigation.NavigationService navigationService) {
        this.navigationService = navigationService;
    }
    @Override
    public String getTitle() {
        return "Timetable";
    }

    @Override
    public Node getView() {
        VBox root = new VBox(16);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(24));

        VBox header = new VBox(4);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Timetable");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Scheduled courses by timeslot and classroom.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        BacktrackResult result = AppState.getInstance().getLastResult();
        switch (result) {
            case BacktrackResult.Success(ClassSchedule solution) -> {
                root.getChildren().addAll(header, createGrid(solution));
                return root;
            }
            case BacktrackResult.NoSolutionFound _ -> {
                VBox card = new VBox(12);
                card.getStyleClass().add("card");
                Label title = new Label("No solution found");
                title.getStyleClass().add("card-title");
                Label msg = new Label("The solver could not find a valid timetable for the current setup. Check constraints and available timeslots/classrooms.");
                msg.getStyleClass().add("card-subtitle");
                msg.setWrapText(true);
                HBox actions = new HBox(10);
                javafx.scene.control.Button setup = new javafx.scene.control.Button("Go to Setup");
                setup.getStyleClass().add("secondary-button");
                setup.setOnAction(e -> navigate(Screen.SETUP));
                javafx.scene.control.Button courses = new javafx.scene.control.Button("Go to Courses");
                courses.getStyleClass().add("secondary-button");
                courses.setOnAction(e -> navigate(Screen.COURSES));
                javafx.scene.control.Button constraints = new javafx.scene.control.Button("Go to Constraints");
                constraints.getStyleClass().add("primary-button");
                constraints.setOnAction(e -> navigate(Screen.CONSTRAINTS));
                actions.getChildren().addAll(setup, courses, constraints);
                card.getChildren().addAll(title, msg, actions);
                root.getChildren().addAll(header, card);
                return root;
            }
            case BacktrackResult.Cancelled _ -> {
                VBox card = new VBox(12);
                card.getStyleClass().add("card");
                Label title = new Label("Solving cancelled");
                title.getStyleClass().add("card-title");
                Label msg = new Label("The solving process was cancelled.");
                msg.getStyleClass().add("card-subtitle");
                msg.setWrapText(true);
                root.getChildren().addAll(header, card);
                return root;
            }
            case null, default -> {
                VBox card = new VBox(12);
                card.getStyleClass().add("card");
                Label title = new Label("No timetable yet");
                title.getStyleClass().add("card-title");
                Label msg = new Label("Configure your scenario in Setup and Courses, then run Solve from the Constraints screen.");
                msg.getStyleClass().add("card-subtitle");
                msg.setWrapText(true);
                HBox actions = new HBox(10);
                javafx.scene.control.Button setup = new javafx.scene.control.Button("Go to Setup");
                setup.getStyleClass().add("secondary-button");
                setup.setOnAction(e -> navigate(Screen.SETUP));
                javafx.scene.control.Button courses = new javafx.scene.control.Button("Go to Courses");
                courses.getStyleClass().add("secondary-button");
                courses.setOnAction(e -> navigate(Screen.COURSES));
                javafx.scene.control.Button constraints = new javafx.scene.control.Button("Go to Constraints");
                constraints.getStyleClass().add("primary-button");
                constraints.setOnAction(e -> navigate(Screen.CONSTRAINTS));
                actions.getChildren().addAll(setup, courses, constraints);
                card.getChildren().addAll(title, msg, actions);
                root.getChildren().addAll(header, card);
                return root;
            }
        }
    }

    private Node createGrid(ClassSchedule schedule) {
        List<Timeslot> timeslots = schedule.assignedCourses().stream()
                .map(ScheduledCourse::timeslot)
                .distinct()
                .sorted(Comparator.comparing(Timeslot::dayOfWeek).thenComparing(Timeslot::startTime))
                .toList();
        List<Classroom> classrooms = schedule.assignedCourses().stream()
                .map(ScheduledCourse::classroom)
                .distinct()
                .sorted(Comparator.comparing(Classroom::name))
                .toList();

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        grid.add(new Label("Timeslot"), 0, 0);
        for (int c = 0; c < classrooms.size(); c++) {
            Label cl = new Label(classrooms.get(c).name());
            cl.getStyleClass().add("card-title");
            grid.add(cl, c + 1, 0);
        }

        for (int r = 0; r < timeslots.size(); r++) {
            Timeslot ts = timeslots.get(r);
            VBox timeBox = new VBox(2);
            Label day = new Label(ts.dayOfWeek().toString());
            Label time = new Label(ts.startTime().toString());
            day.getStyleClass().add("timetable-time-day");
            time.getStyleClass().add("timetable-time");
            timeBox.getChildren().addAll(day, time);
            timeBox.getStyleClass().add("timetable-time-cell");
            grid.add(timeBox, 0, r + 1);
            for (int c = 0; c < classrooms.size(); c++) {
                Classroom cr = classrooms.get(c);
                Node cell = createCell(schedule, ts, cr);
                grid.add(cell, c + 1, r + 1);
            }
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.getStyleClass().add("timetable-scroll");
        return sp;
    }

    private void navigate(Screen screen) {
        if (navigationService != null) {
            navigationService.navigateTo(screen);
        }
    }

    private Node createCell(ClassSchedule schedule, Timeslot ts, Classroom cr) {
        for (ScheduledCourse sc : schedule.assignedCourses()) {
            if (sc.timeslot().equals(ts) && sc.classroom().equals(cr)) {
                VBox cell = new VBox(4);
                cell.getStyleClass().add("timetable-cell");
                Label subject = new Label(sc.course().subject().value());
                subject.getStyleClass().add("timetable-subject");
                Label teacher = new Label(sc.course().teacher().value());
                teacher.getStyleClass().add("timetable-teacher");
                String groups = sc.course().studentGroups().stream()
                        .map(g -> g.value())
                        .reduce((a,b) -> a + ", " + b)
                        .orElse("");
                if (!groups.isEmpty()) {
                    Label grp = new Label(groups);
                    grp.getStyleClass().add("timetable-groups");
                    grp.setWrapText(true);
                    cell.getChildren().addAll(subject, teacher, grp);
                } else {
                    cell.getChildren().addAll(subject, teacher);
                }
                return cell;
            }
        }
        Label empty = new Label("");
        empty.getStyleClass().add("timetable-cell-empty");
        return empty;
    }
}
