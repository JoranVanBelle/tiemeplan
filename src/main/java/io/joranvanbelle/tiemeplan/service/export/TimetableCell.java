package io.joranvanbelle.tiemeplan.service.export;

import java.util.List;

public record TimetableCell(SlotState state, List<TimetableCellEntry> entries) {

    public TimetableCell {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static TimetableCell notATimeslot() {
        return new TimetableCell(SlotState.NOT_A_TIMESLOT, List.of());
    }

    public static TimetableCell free() {
        return new TimetableCell(SlotState.FREE, List.of());
    }

    public static TimetableCell blocked() {
        return new TimetableCell(SlotState.BLOCKED, List.of());
    }

    public static TimetableCell course(List<TimetableCellEntry> entries) {
        return new TimetableCell(entries.size() > 1 ? SlotState.CONFLICT : SlotState.COURSE, entries);
    }

    public static TimetableCell master(List<TimetableCellEntry> entries) {
        return new TimetableCell(SlotState.COURSE, entries);
    }

    public boolean hasCourses() {
        return state == SlotState.COURSE || state == SlotState.CONFLICT;
    }
}
