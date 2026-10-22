package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StudentGroupDto(
        @JsonProperty("name") String name,
        @JsonProperty("size") int size) {
}
