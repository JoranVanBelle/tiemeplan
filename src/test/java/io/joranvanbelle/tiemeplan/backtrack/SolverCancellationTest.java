package io.joranvanbelle.tiemeplan.backtrack;

import io.joranvanbelle.tiemeplan.util.CancellationToken;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolverCancellationTest {

    @Test
    void cancelledReturnsCancelledState() {
        var timeslots = List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        var classrooms = List.of(new Classroom("room1", 30, List.of()));
        var courses = new ArrayList<Course>();
        for (int i = 0; i < 20; i++) {
            var id = UUID.randomUUID().toString();
            courses.add(new Course(id, new Teacher("T" + i), new Subject("S" + i), List.of(new StudentGroup("G" + i, 10))));
        }

        var solver = new Solver(classrooms, timeslots);
        var token = new CancellationToken();
        token.cancel();

        var result = solver.solve(courses, token);

        assertThat(result).isInstanceOf(BacktrackResult.Cancelled.class);
    }
}
