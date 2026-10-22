package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ScenarioDto(
        @JsonProperty("schemaVersion") int schemaVersion,
        @JsonProperty("courses") List<CourseDto> courses,
        @JsonProperty("classrooms") List<ClassroomDto> classrooms,
        @JsonProperty("timeslots") List<TimeslotDto> timeslots,
        @JsonProperty("teachers") List<TeacherDto> teachers,
        @JsonProperty("subjects") List<SubjectDto> subjects,
        @JsonProperty("studentGroups") List<StudentGroupDto> studentGroups) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public ScenarioDto {
        courses = courses == null ? List.of() : List.copyOf(courses);
        classrooms = classrooms == null ? List.of() : List.copyOf(classrooms);
        timeslots = timeslots == null ? List.of() : List.copyOf(timeslots);
        teachers = teachers == null ? List.of() : List.copyOf(teachers);
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
        studentGroups = studentGroups == null ? List.of() : List.copyOf(studentGroups);
    }

    public static ScenarioDto empty() {
        return new ScenarioDto(CURRENT_SCHEMA_VERSION, List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of());
    }
}
