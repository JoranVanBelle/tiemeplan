package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CourseDto(
        @JsonProperty("teacher") TeacherDto teacher,
        @JsonProperty("subject") SubjectDto subject,
        @JsonProperty("studentGroups") List<StudentGroupDto> studentGroups) {

    public CourseDto {
        studentGroups = studentGroups == null ? List.of() : List.copyOf(studentGroups);
    }
}
