package io.joranvanbelle.tiemeplan.service.export;

import java.time.Instant;
import java.util.List;

public record TimetableExportDocument(
        String title,
        Instant generatedAt,
        ExportAudience audience,
        ExportSelection selection,
        String filterSummary,
        List<TimetableSheet> sheets) {

    public TimetableExportDocument {
        sheets = sheets == null ? List.of() : List.copyOf(sheets);
    }
}
