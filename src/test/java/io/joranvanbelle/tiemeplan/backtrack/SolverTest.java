package io.joranvanbelle.tiemeplan.backtrack;

import io.joranvanbelle.tiemeplan.ClassScheduleAssert;
import io.joranvanbelle.tiemeplan.CourseMother;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverTest {

    @Test
    void findFirstResult_oneClassRoom() {
        // given
        var availableClassRooms = List.of(new Classroom("room1", 100, List.of()));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(12, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup("1A", 10)
                        .build(),
                CourseMother.newCourse()
                        .teacher("Ms. Johansen")
                        .subject("Math")
                        .studentGroup("2A", 10)
                        .build(),
                CourseMother.newCourse()
                        .teacher("Mr. Smith")
                        .subject("PE")
                        .studentGroup("3A", 10)
                        .build());
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(3)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup("1A", 10)
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Ms. Johansen")
                                                .subject("Math")
                                                .studentGroup("2A", 10)
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(12, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Smith")
                                                .subject("PE")
                                                .studentGroup("3A", 10)
                                                .build())
                                        .build()));
    }

    @Test
    void findResult_fillUpClassRoomsFirst() {
        // given
        var availableClassRooms = List.of(
                new Classroom("room1", 100, List.of()),
                new Classroom("room2", 100, List.of()));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(12, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup("1A", 10)
                        .build(),
                CourseMother.newCourse()
                        .teacher("Ms. Johansen")
                        .subject("Math")
                        .studentGroup("2A", 10)
                        .build(),
                CourseMother.newCourse()
                        .teacher("Mr. Smith")
                        .subject("PE")
                        .studentGroup("3A", 10)
                        .build());
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(3)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup("1A", 10)
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room2", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Ms. Johansen")
                                                .subject("Math")
                                                .studentGroup("2A", 10)
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 100)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Smith")
                                                .subject("PE")
                                                .studentGroup("3A", 10)
                                                .build())
                                        .build()));
    }

    @Test
    void assignStudentGroupToCorrectClassroom() {
        // given
        var availableClassRooms = List.of(
                new Classroom("room1", 10, List.of()),
                new Classroom("room2", 20, List.of()),
                new Classroom("room3", 10, List.of()));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup("1A", 15)
                        .build());
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(1)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room2", 20)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup("1A", 15)
                                                .build())
                                        .build()));
    }

    @Test
    void assignStudentGroupToCorrectClassroom_multipleStudentGroupTogether() {
        // given
        var availableClassRooms = List.of(
                new Classroom("room1", 10, List.of()),
                new Classroom("room2", 20, List.of()));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup(
                                List.of(new StudentGroup("1A", 10),
                                        new StudentGroup("2B", 10)))
                        .build());
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(1)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room2", 20)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup(
                                                        List.of(new StudentGroup("1A", 10),
                                                                new StudentGroup("2B", 10)))
                                                .build())
                                        .build()));
    }

    @Test
    void scheduleStudentGroupsCorrectly() {
        // given
        var availableClassRooms = List.of(
                new Classroom("room1", 10, List.of()),
                new Classroom("room2", 20, List.of()));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        StudentGroup studentGroup1 = new StudentGroup("1A", 10);
        StudentGroup studentGroup2 = new StudentGroup("2B", 10);
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Ms. Johansen")
                        .subject("Math")
                        .studentGroup(
                                List.of(studentGroup2))
                        .build(),
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup(
                                List.of(studentGroup1, studentGroup2))
                        .build(),
                CourseMother.newCourse()
                        .teacher("Ms. Johansen")
                        .subject("Math")
                        .studentGroup(List.of(studentGroup1))
                        .build());
        var result = solver.solve(courses);

        // then
        // todo - more favorable would be to schedule Ms. Johansen at 10 and at 11 so
        // that she had no gaps in her schedule
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(3)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 10)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Ms. Johansen")
                                                .subject("Math")
                                                .studentGroup(
                                                        List.of(studentGroup2))
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room2", 20)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup(
                                                        List.of(studentGroup1,
                                                                studentGroup2))
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .alwaysFreeClassroom("room1", 10)
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Ms. Johansen")
                                                .subject("Math")
                                                .studentGroup(
                                                        List.of(studentGroup1))
                                                .build())
                                        .build()));
    }

    @Test
    void onlyUseAvailableClassRooms() {

        // given
        var availableClassRooms = List.of(
                new Classroom("room1", 100,
                        List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)))),
                new Classroom("room2", 100, List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)))),
                new Classroom("room3", 100, List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)))));
        var availableTimeslots = List.of(
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)));
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var courses = List.of(
                CourseMother.newCourse()
                        .teacher("Mr. Johnson")
                        .subject("English")
                        .studentGroup("1A", 10)
                        .build(),
                CourseMother.newCourse()
                        .teacher("Ms. Johansen")
                        .subject("Math")
                        .studentGroup("2A", 10)
                        .build());
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.Success.class,
                res -> ClassScheduleAssert.assertThat(res.solution())
                        .hasNumberOfCourses(2)
                        .containsOnlyScheduledCoursed(
                                ScheduledCourse.newBuilder()
                                        .classroom("room3", 100,
                                                List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))))
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Mr. Johnson")
                                                .subject("English")
                                                .studentGroup("1A", 10)
                                                .build())
                                        .build(),
                                ScheduledCourse.newBuilder()
                                        .classroom("room2", 100,
                                                List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))))
                                        .timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))
                                        .course(Course.newBuilder()
                                                .teacher("Ms. Johansen")
                                                .subject("Math")
                                                .studentGroup("2A", 10)
                                                .build())
                                        .build()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCombinations")
    void noResult_whenNotPossible(
            String name,
            List<Classroom> availableClassRooms,
            List<Timeslot> availableTimeslots,
            List<Course> courses) {
        // given
        var solver = new Solver(availableClassRooms, availableTimeslots);

        // when
        var result = solver.solve(courses);

        // then
        assertThat(result).isInstanceOfSatisfying(BacktrackResult.NoSolutionFound.class,
                res -> ClassScheduleAssert.assertThat(res.previousState())
                        .hasNoCourses());
    }

    private static Stream<Arguments> invalidCombinations() {
        return Stream.of(
                Arguments.of("not enough rooms", List.of(new Classroom("room1", 100, List.of())),
                        List.of(
                                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))),
                        List.of(
                                CourseMother.newCourse()
                                        .teacher("Mr. Johnson")
                                        .subject("English")
                                        .studentGroup("1A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Ms. Johansen")
                                        .subject("Math")
                                        .studentGroup("2A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Mr. Smith")
                                        .subject("PE")
                                        .studentGroup("3A", 10)
                                        .build())),
                Arguments.of("not enough timeslots", List.of(
                        new Classroom("room1", 100, List.of()),
                        new Classroom("room2", 100, List.of())),
                        List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))),
                        List.of(
                                CourseMother.newCourse()
                                        .teacher("Mr. Johnson")
                                        .subject("English")
                                        .studentGroup("1A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Ms. Johansen")
                                        .subject("Math")
                                        .studentGroup("2A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Mr. Smith")
                                        .subject("PE")
                                        .studentGroup("3A", 10)
                                        .build())),
                Arguments.of("not enough teachers", List.of(new Classroom("room1", 100, List.of())),
                        List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))),
                        List.of(
                                CourseMother.newCourse()
                                        .teacher("Mr. Johnson")
                                        .subject("English")
                                        .studentGroup("1A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Ms. Johnson")
                                        .subject("Math")
                                        .studentGroup("2A", 10)
                                        .build())),
                Arguments.of("classroom cannot hold students",
                        List.of(new Classroom("room1", 10, List.of())),
                        List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))),
                        List.of(
                                CourseMother.newCourse()
                                        .teacher("Mr. Johnson")
                                        .subject("English")
                                        .studentGroup(List.of(new StudentGroup("1A", 7),
                                                new StudentGroup("2B", 8)))
                                        .build())),
                Arguments.of("correct classroom not available", List.of(
                        new Classroom("room1", 15,
                                List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                                        new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0)))),
                        new Classroom("room2", 5, List.of())),
                        List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
                                new Timeslot(DayOfWeek.MONDAY, LocalTime.of(11, 0))),
                        List.of(
                                CourseMother.newCourse()
                                        .teacher("Mr. Johnson")
                                        .subject("English")
                                        .studentGroup("1A", 10)
                                        .build(),
                                CourseMother.newCourse()
                                        .teacher("Ms. Johansen")
                                        .subject("Math")
                                        .studentGroup("2A", 10)
                                        .build())));
    }
}
