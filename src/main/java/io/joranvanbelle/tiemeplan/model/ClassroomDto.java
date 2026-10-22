package io.joranvanbelle.tiemeplan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClassroomDto(
        @JsonProperty("name") String name,
        @JsonProperty("capacity") int capacity,
        @JsonProperty("occupiedTimeslots") List<TimeslotDto> occupiedTimeslots) {

    public ClassroomDto {
        occupiedTimeslots = occupiedTimeslots == null ? List.of() : List.copyOf(occupiedTimeslots);
    }
}
