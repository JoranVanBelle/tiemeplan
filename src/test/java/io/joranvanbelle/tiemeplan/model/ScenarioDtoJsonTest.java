package io.joranvanbelle.tiemeplan.model;

import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioDtoJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serializesWithFriendlyJsonPropertyNames() {
        var id = UUID.randomUUID().toString();
        var dto = new ScenarioDto(ScenarioDto.CURRENT_SCHEMA_VERSION,
                List.of(new CourseDto(id, new TeacherDto("Ms. Smith"), new SubjectDto("Math"),
                        List.of(new StudentGroupDto("1A", 10)), null)),
                List.of(new ClassroomDto("room1", 30,
                        List.of(new TimeslotDto(DayOfWeek.MONDAY, LocalTime.of(10, 0))))),
                List.of(new TimeslotDto(DayOfWeek.MONDAY, LocalTime.of(10, 0))),
                List.of(new TeacherDto("Ms. Smith")),
                List.of(new SubjectDto("Math")),
                List.of(new StudentGroupDto("1A", 10)));

        var json = mapper.writeValueAsString(dto);

        assertThat(json).isEqualToIgnoringWhitespace("""
                {
                  "schemaVersion" : 1,
                  "appVersion" : null,
                  "courses" : [ {
                    "id" : "%s",
                    "teacher" : {
                      "value" : "Ms. Smith"
                    },
                    "subject" : {
                      "value" : "Math"
                    },
                    "studentGroups" : [ {
                      "name" : "1A",
                      "size" : 10
                    } ]
                  } ],
                  "classrooms" : [ {
                    "name" : "room1",
                    "capacity" : 30,
                    "occupiedTimeslots" : [ {
                      "dayOfWeek" : "MONDAY",
                      "startTime" : "10:00:00"
                    } ]
                  } ],
                  "timeslots" : [ {
                    "dayOfWeek" : "MONDAY",
                    "startTime" : "10:00:00"
                  } ],
                  "teachers" : [ {
                    "value" : "Ms. Smith"
                  } ],
                  "subjects" : [ {
                    "value" : "Math"
                  } ],
                  "studentGroups" : [ {
                    "name" : "1A",
                    "size" : 10
                  } ]
                }""".formatted(id));
        assertThat(mapper.readValue(json, ScenarioDto.class)).isEqualTo(dto);
    }

    @Test
    void supportV1Schema() {
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

        assertThat(json).isEqualToIgnoringWhitespace("""
                {
                  "schemaVersion" : 1,
                  "appVersion" : null,
                  "courses" : [ {
                    "id" : "%s",
                    "teacher" : {
                      "value" : "Ms. Smith"
                    },
                    "subject" : {
                      "value" : "Math"
                    },
                    "studentGroups" : [ {
                      "name" : "1A",
                      "size" : 10
                    } ]
                  } ],
                  "classrooms" : [ {
                    "name" : "room1",
                    "capacity" : 30,
                    "occupiedTimeslots" : [ {
                      "dayOfWeek" : "MONDAY",
                      "startTime" : "10:00:00"
                    } ]
                  } ],
                  "timeslots" : [ {
                    "dayOfWeek" : "MONDAY",
                    "startTime" : "10:00:00"
                  } ],
                  "teachers" : [ {
                    "value" : "Ms. Smith"
                  } ],
                  "subjects" : [ {
                    "value" : "Math"
                  } ],
                  "studentGroups" : [ {
                    "name" : "1A",
                    "size" : 10
                  } ]
                }""".formatted(dto.courses().getFirst().id()));
        assertThat(mapper.readValue(json, ScenarioDto.class)).isEqualTo(dto);
    }
}
