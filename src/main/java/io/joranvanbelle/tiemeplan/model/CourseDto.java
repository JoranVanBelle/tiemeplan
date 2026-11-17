package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record CourseDto(
        @JsonProperty("id") String id,
        @JsonProperty("teacher") TeacherDto teacher,
        @JsonProperty("subject") SubjectDto subject,
        @JsonProperty("studentGroups") List<StudentGroupDto> studentGroups,
        @JsonProperty("nextCourseId") String nextCourseId) {

    public CourseDto(
            @JsonProperty("teacher") TeacherDto teacher,
            @JsonProperty("subject") SubjectDto subject,
            @JsonProperty("studentGroups") List<StudentGroupDto> studentGroups
    ) {
        this(UUID.randomUUID().toString(), teacher, subject, studentGroups, null);
    }

    public CourseDto {
        studentGroups = studentGroups == null ? List.of() : List.copyOf(studentGroups);
    }
}
