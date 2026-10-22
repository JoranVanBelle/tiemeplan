package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TeacherDto(@JsonProperty("value") String value) {
}
