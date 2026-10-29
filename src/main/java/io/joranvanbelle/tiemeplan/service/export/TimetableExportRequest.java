package io.joranvanbelle.tiemeplan.service.export;

public record TimetableExportRequest(
        ExportFormat format,
        ExportAudience audience,
        ExportSelection selection,
        String entityName,
        ExportFilters filters,
        boolean applyCurrentFilters,
        double pngScale,
        boolean useCurrentTheme) {

    public TimetableExportRequest {
        format = format == null ? ExportFormat.PDF : format;
        audience = audience == null ? ExportAudience.MASTER : audience;
        selection = selection == null ? ExportSelection.ALL : selection;
        filters = filters == null ? ExportFilters.none() : filters;
        if (pngScale <= 0) {
            pngScale = 2.0;
        }
    }

    public static TimetableExportRequest pdf(ExportAudience audience, ExportSelection selection, String entityName) {
        return new TimetableExportRequest(ExportFormat.PDF, audience, selection, entityName,
                ExportFilters.none(), false, 2.0, true);
    }

    public boolean isBulkPng() {
        return format == ExportFormat.PNG && selection == ExportSelection.ALL && audience != ExportAudience.MASTER;
    }

    public boolean isSingleEntity() {
        return audience != ExportAudience.MASTER && selection == ExportSelection.SINGLE;
    }
}
