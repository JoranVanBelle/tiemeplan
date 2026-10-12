package io.joranvanbelle.tiemeplan.backtrack;

import io.joranvanbelle.tiemeplan.CourseMother;
import io.joranvanbelle.tiemeplan.ClassScheduleAssert;
import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule.ScheduledCourse;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Timeslot;
import net.bytebuddy.asm.MemberSubstitution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverTest {

    @Test
    void findFirstResult_oneClassRoom() {
        // given
        var availableClassRooms = List.of(new Classroom("room1"));
        var availableTimeslots = List.of(new Timeslot(LocalTime.of(10, 0)), new Timeslot(LocalTime.of(11, 0)), new Timeslot(LocalTime.of(12, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse().teacher("Mr. Johnson").subject("English").studentGroup("1A").build(),
                CourseMother.newCourse().teacher("Ms. Johansen").subject("Math").studentGroup("2A").build(),
                CourseMother.newCourse().teacher("Mr. Smith").subject("PE").studentGroup("3A").build()
        );
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(3)
                        .containsOnlyScheduledCoursed(
                                new ScheduledCourse("room1", LocalTime.of(10, 0), "Mr. Johnson", "English", "1A"),
                                new ScheduledCourse("room1", LocalTime.of(11, 0), "Ms. Johansen", "Math", "2A"),
                                new ScheduledCourse("room1", LocalTime.of(12, 0), "Mr. Smith", "PE", "3A")
                        )
        );
    }

    @Test
    void findResult_fillUpClassRoomsFirst() {
        // given
        var availableClassRooms = List.of(new Classroom("room1"), new Classroom("room2"));
        var availableTimeslots = List.of(new Timeslot(LocalTime.of(10, 0)), new Timeslot(LocalTime.of(11, 0)), new Timeslot(LocalTime.of(12, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse().teacher("Mr. Johnson").subject("English").studentGroup("1A").build(),
                CourseMother.newCourse().teacher("Ms. Johansen").subject("Math").studentGroup("2A").build(),
                CourseMother.newCourse().teacher("Mr. Smith").subject("PE").studentGroup("3A").build()
        );
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(3)
                        .containsOnlyScheduledCoursed(
                                new ScheduledCourse("room1", LocalTime.of(10, 0), "Mr. Johnson", "English", "1A"),
                                new ScheduledCourse("room2", LocalTime.of(10, 0), "Ms. Johansen", "Math", "2A"),
                                new ScheduledCourse("room1", LocalTime.of(11, 0), "Mr. Smith", "PE", "3A")
                        )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCombinations")
    void noResult_whenNotPossible(
            String name,
            List<Classroom> availableClassRooms,
            List<Timeslot> availableTimeslots,
            List<Course> courses
    ) {
        // given
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.NoSolutionFound.class,
                res -> ClassScheduleAssert.assertThat(res.previousState())
                        .hasNoCourses()
        );
    }

    private static Stream<Arguments> invalidCombinations() {
        return Stream.of(
          Arguments.of("not enough rooms",  List.of(new Classroom("room1")), List.of(new Timeslot(LocalTime.of(10, 0)), new Timeslot(LocalTime.of(11, 0))), List.of(
                  CourseMother.newCourse().teacher("Mr. Johnson").subject("English").studentGroup("1A").build(),
                  CourseMother.newCourse().teacher("Ms. Johansen").subject("Math").studentGroup("2A").build(),
                  CourseMother.newCourse().teacher("Mr. Smith").subject("PE").studentGroup("3A").build()
          )),
                Arguments.of("not enough timeslots",  List.of(new Classroom("room1"), new Classroom("room2")), List.of(new Timeslot(LocalTime.of(10, 0))), List.of(
                        CourseMother.newCourse().teacher("Mr. Johnson").subject("English").studentGroup("1A").build(),
                        CourseMother.newCourse().teacher("Ms. Johansen").subject("Math").studentGroup("2A").build(),
                        CourseMother.newCourse().teacher("Mr. Smith").subject("PE").studentGroup("3A").build()
                )),
                Arguments.of("not enough teachers",  List.of(new Classroom("room1")), List.of(new Timeslot(LocalTime.of(10, 0))), List.of(
                        CourseMother.newCourse().teacher("Mr. Johnson").subject("English").studentGroup("1A").build(),
                        CourseMother.newCourse().teacher("Ms. Johnson").subject("Math").studentGroup("2A").build()
                ))
        );
    }
}
