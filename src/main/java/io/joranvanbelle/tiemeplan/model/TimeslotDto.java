package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record TimeslotDto(
        @JsonProperty("dayOfWeek") DayOfWeek dayOfWeek,
        @JsonProperty("startTime") LocalTime startTime) {
}
