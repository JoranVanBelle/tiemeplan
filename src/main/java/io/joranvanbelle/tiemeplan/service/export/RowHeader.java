package io.joranvanbelle.tiemeplan.service.export;

import java.time.LocalTime;

public record RowHeader(LocalTime startTime, String label) {
}
