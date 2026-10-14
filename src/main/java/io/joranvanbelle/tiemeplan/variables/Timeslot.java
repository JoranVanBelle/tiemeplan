package io.joranvanbelle.tiemeplan.variables;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Timeslot(DayOfWeek dayOfWeek, LocalTime startTime) {
}
