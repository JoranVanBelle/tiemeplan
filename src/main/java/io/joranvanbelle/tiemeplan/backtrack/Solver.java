package io.joranvanbelle.tiemeplan.backtrack;

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

        return solve(0, totalCourses, ClassSchedule.empty());
    }

    private BacktrackResult solve(int courseIdx, List<Course> totalCourses, ClassSchedule classSchedule) {

        if (courseIdx == totalCourses.size()) {
            return new BacktrackResult.Success(classSchedule);
        }

        var currentCourse = totalCourses.get(courseIdx);

        for (var timeslot : timeslots) {
            for (var classRoom : classRooms) {

                if (classSchedule.isValid(classRoom, timeslot, currentCourse)) {

                    classSchedule.assign(classRoom, timeslot, currentCourse);

                    if (solve(courseIdx + 1, totalCourses, classSchedule) instanceof BacktrackResult.Success success) {
                        return success;
                    }

                    // new state was not valid
                    classSchedule.unassign(classRoom, timeslot, currentCourse);
                }

            }
        }

        return new BacktrackResult.NoSolutionFound(classSchedule);
    }
}
