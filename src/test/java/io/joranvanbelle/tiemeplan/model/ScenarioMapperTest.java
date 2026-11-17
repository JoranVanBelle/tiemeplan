package io.joranvanbelle.tiemeplan.model;

import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioMapperTest {

    @Test
    void roundTripsDomainToDtoKeepingOrder() {
        var timeslot = new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0));
        var classroom = new Classroom("room1", 30, List.of(timeslot));
        var group = new StudentGroup("1A", 10);
        var course = new Course(UUID.randomUUID().toString(), new Teacher("Ms. Smith"), new Subject("Math"), List.of(group));
        var scenario = new ScenarioDto(ScenarioDto.CURRENT_SCHEMA_VERSION,
                List.of(ScenarioMapper.toDto(course)),
                List.of(ScenarioMapper.toDto(classroom)),
                List.of(ScenarioMapper.toDto(timeslot)),
                List.of(ScenarioMapper.toDto(new Teacher("Ms. Smith"))),
                List.of(ScenarioMapper.toDto(new Subject("Math"))),
                List.of(ScenarioMapper.toDto(group)));

        assertThat(ScenarioMapper.toDomainCourses(scenario.courses())).containsExactly(course);
        assertThat(ScenarioMapper.toDomainClassrooms(scenario.classrooms())).containsExactly(classroom);
        assertThat(ScenarioMapper.toDomainTimeslots(scenario.timeslots())).containsExactly(timeslot);
        assertThat(ScenarioMapper.toDomainTeachers(scenario.teachers())).containsExactly(new Teacher("Ms. Smith"));
        assertThat(ScenarioMapper.toDomainSubjects(scenario.subjects())).containsExactly(new Subject("Math"));
        assertThat(ScenarioMapper.toDomainStudentGroups(scenario.studentGroups())).containsExactly(group);
    }

    @Test
    void copiesListsDefensively() {
        var dto = new ScenarioDto(1, null, null, null, null, null, null);

        assertThat(dto.courses()).isEmpty();
        assertThat(dto.classrooms()).isEmpty();
        assertThat(dto.timeslots()).isEmpty();
        assertThat(dto.teachers()).isEmpty();
        assertThat(dto.subjects()).isEmpty();
        assertThat(dto.studentGroups()).isEmpty();
    }
}
