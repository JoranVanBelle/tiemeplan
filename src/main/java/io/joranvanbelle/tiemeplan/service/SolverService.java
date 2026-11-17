package io.joranvanbelle.tiemeplan.service;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.backtrack.Solver;
import io.joranvanbelle.tiemeplan.util.CancellationToken;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.util.Comparator;
import java.util.List;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class SolverService extends Service<BacktrackResult> {

    private List<Classroom> classrooms;
    private List<Timeslot> timeslots;
    private List<Course> courses;
    private CancellationToken cancellationToken;

    public void setClassrooms(List<Classroom> classrooms) {
        this.classrooms = classrooms;
    }

    public void setTimeslots(List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public void reset(List<Classroom> classrooms, List<Timeslot> timeslots, List<Course> courses) {
        setClassrooms(classrooms);
        setTimeslots(timeslots);
        setCourses(courses);
    }

    public void requestCancel() {
        if (cancellationToken != null) {
            cancellationToken.cancel();
        }
        cancel();
    }

    @Override
    protected Task<BacktrackResult> createTask() {
        var solverClassrooms = classrooms == null ? List.<Classroom>of() : List.copyOf(classrooms);
        var solverTimeslots = timeslots == null ? List.<Timeslot>of() : timeslots.stream()
                .sorted(Comparator.comparing(Timeslot::dayOfWeek)
                        .thenComparing(Timeslot::startTime))
                .toList();

        // todo - group courses next to each other when needed
        var solverCourses = courses == null ? List.<Course>of() : List.copyOf(courses);
        cancellationToken = new CancellationToken();

        return new Task<>() {
            @Override
            protected BacktrackResult call() {
                try {
                    return new Solver(solverClassrooms, solverTimeslots).solve(solverCourses, cancellationToken);
                } catch (CancellationToken.CancellationException e) {
                    return new BacktrackResult.Cancelled(
                            io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.empty());
                }
            }
        };
    }
}
