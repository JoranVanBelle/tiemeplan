package io.joranvanbelle.tiemeplan.model;

import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioDtoJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serializesWithFriendlyJsonPropertyNames() {
        var dto = new ScenarioDto(ScenarioDto.CURRENT_SCHEMA_VERSION,
                List.of(new CourseDto(new TeacherDto("Ms. Smith"), new SubjectDto("Math"),
                        List.of(new StudentGroupDto("1A", 10)))),
                List.of(new ClassroomDto("room1", 30,
                        List.of(new TimeslotDto(DayOfWeek.MONDAY, LocalTime.of(10, 0))))),
                List.of(new TimeslotDto(DayOfWeek.MONDAY, LocalTime.of(10, 0))),
                List.of(new TeacherDto("Ms. Smith")),
                List.of(new SubjectDto("Math")),
                List.of(new StudentGroupDto("1A", 10)));

        var json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"schemaVersion\"").contains("\"courses\"").contains("\"classrooms\"")
                .contains("\"timeslots\"").contains("\"teachers\"").contains("\"subjects\"")
                .contains("\"studentGroups\"").contains("\"value\"").contains("\"dayOfWeek\"")
                .contains("\"startTime\"");
        assertThat(mapper.readValue(json, ScenarioDto.class)).isEqualTo(dto);
    }
}
