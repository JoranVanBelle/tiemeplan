package io.joranvanbelle.tiemeplan.service.export;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record TimetableSheet(
        ExportAudience audience,
        String entityName,
        String title,
        String subtitle,
        List<DayOfWeek> days,
        List<RowHeader> rowHeaders,
        Map<CellKey, TimetableCell> cells,
        List<String> notes) {

    public TimetableSheet {
        days = days == null ? List.of() : List.copyOf(days);
        rowHeaders = rowHeaders == null ? List.of() : List.copyOf(rowHeaders);
        cells = cells == null ? Map.of() : Map.copyOf(cells);
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    public TimetableCell cell(int rowIndex, int columnIndex) {
        return cells.getOrDefault(new CellKey(rowIndex, columnIndex), TimetableCell.notATimeslot());
    }

    public List<String> columnHeaders(Locale locale) {
        List<String> headers = new ArrayList<>();
        for (DayOfWeek day : days) {
            headers.add(ExportText.dayName(day, locale));
        }
        return List.copyOf(headers);
    }

    public int scheduledCourseCount() {
        return cells.values().stream()
                .filter(TimetableCell::hasCourses)
                .mapToInt(cell -> cell.entries().size())
                .sum();
    }
}
