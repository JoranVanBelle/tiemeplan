package io.joranvanbelle.tiemeplan.backtrack;

import io.joranvanbelle.tiemeplan.util.CancellationToken;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.util.List;

/**
 * take in all the variables:
 * - Classroom
 * - StudentGroup
 * - Subject
 * - Teacher
 * - Timeslot
 * run through all possible combinations until each group is able to take their
 * course
 * Each subject should be given X amount of times
 */
public class Solver {

    private final List<Classroom> classRooms;
    private final List<Timeslot> timeslots;

    public Solver(List<Classroom> classRooms, List<Timeslot> timeslots) {
        this.classRooms = classRooms;
        this.timeslots = timeslots;
    }

    public BacktrackResult solve(List<Course> totalCourses) {
        return solve(totalCourses, null);
    }

    public BacktrackResult solve(List<Course> totalCourses, CancellationToken cancellationToken) {

        var occupiedClassRooms = classRooms.stream()
                .filter(c -> !c.occupiedTimeslots().isEmpty())
                .toList();

        return solve(0, totalCourses, ClassSchedule.empty().addUnavailability(occupiedClassRooms), cancellationToken);
    }

    private BacktrackResult solve(int courseIdx, List<Course> totalCourses, ClassSchedule classSchedule,
            CancellationToken cancellationToken) {

        if (cancellationToken != null && cancellationToken.isCancelled()) {
            return new BacktrackResult.Cancelled(classSchedule);
        }

        if (courseIdx == totalCourses.size()) {
            return new BacktrackResult.Success(classSchedule);
        }

        var currentCourse = totalCourses.get(courseIdx);

        for (var timeslot : timeslots) {
            for (var classRoom : classRooms) {

                if (cancellationToken != null && cancellationToken.isCancelled()) {
                    return new BacktrackResult.Cancelled(classSchedule);
                }

                if (classSchedule.isValid(classRoom, timeslot, currentCourse)) {

                    classSchedule.assign(classRoom, timeslot, currentCourse);

                    var result = solve(courseIdx + 1, totalCourses, classSchedule, cancellationToken);
                    if (result instanceof BacktrackResult.Success success) {
                        return success;
                    }
                    if (result instanceof BacktrackResult.Cancelled cancelled) {
                        classSchedule.unassign(classRoom, timeslot, currentCourse);
                        return cancelled;
                    }

                    // new state was not valid
                    classSchedule.unassign(classRoom, timeslot, currentCourse);
                }

            }
        }

        if (cancellationToken != null && cancellationToken.isCancelled()) {
            return new BacktrackResult.Cancelled(classSchedule);
        }

        return new BacktrackResult.NoSolutionFound(classSchedule);
    }
}
