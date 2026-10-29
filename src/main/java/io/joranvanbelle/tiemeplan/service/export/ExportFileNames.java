package io.joranvanbelle.tiemeplan.service.export;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ExportFileNames {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    private ExportFileNames() {
    }

    public static String suggest(TimetableExportRequest request, Instant now, ZoneId zoneId) {
        return "timetable-" + audienceSlug(request) + "-"
                + STAMP.format(now.atZone(zoneId))
                + extension(request);
    }

    public static String extension(TimetableExportRequest request) {
        return switch (request.format()) {
            case PDF -> ".pdf";
            case CSV -> ".csv";
            case PNG -> request.isBulkPng() ? ".zip" : ".png";
        };
    }

    public static String audienceSlug(TimetableExportRequest request) {
        String audience = switch (request.audience()) {
            case MASTER -> "master";
            case TEACHER -> "teachers";
            case STUDENT_GROUP -> "student-groups";
            case CLASSROOM -> "classrooms";
        };
        if (request.isSingleEntity() && request.entityName() != null && !request.entityName().isBlank()) {
            audience += "-" + sanitize(request.entityName());
        }
        return audience;
    }

    public static Path ensureExtension(Path path, String extension) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            return path;
        }
        return path.resolveSibling(fileName + extension);
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "entity";
        }
        String sanitized = value.strip()
                .replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "-")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        if (sanitized.isBlank()) {
            return "entity";
        }
        if (sanitized.length() > 80) {
            sanitized = sanitized.substring(0, 80).replaceAll("-+$", "");
        }
        return sanitized;
    }
}
