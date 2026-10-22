package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubjectDto(@JsonProperty("value") String value) {
}
