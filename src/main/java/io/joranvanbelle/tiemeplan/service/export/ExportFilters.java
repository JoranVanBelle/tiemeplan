package io.joranvanbelle.tiemeplan.service.export;

import java.util.ArrayList;
import java.util.List;

public record ExportFilters(String teacher, String studentGroup, String classroom, String subject) {

    public static ExportFilters none() {
        return new ExportFilters(null, null, null, null);
    }

    public boolean isEmpty() {
        return isBlank(teacher) && isBlank(studentGroup) && isBlank(classroom) && isBlank(subject);
    }

    public ExportFilters withoutAudienceFilter(ExportAudience audience) {
        return switch (audience) {
            case TEACHER -> new ExportFilters(null, studentGroup, classroom, subject);
            case STUDENT_GROUP -> new ExportFilters(teacher, null, classroom, subject);
            case CLASSROOM -> new ExportFilters(teacher, studentGroup, null, subject);
            case MASTER -> this;
        };
    }

    public boolean matches(TimetableCellEntry entry) {
        if (!isBlank(teacher) && !equalsIgnoreCase(teacher, entry.teacher())) {
            return false;
        }
        if (!isBlank(classroom) && !equalsIgnoreCase(classroom, entry.classroom())) {
            return false;
        }
        if (!isBlank(subject) && !equalsIgnoreCase(subject, entry.subject())) {
            return false;
        }
        if (!isBlank(studentGroup) && entry.studentGroups().stream()
                .noneMatch(group -> equalsIgnoreCase(studentGroup, group))) {
            return false;
        }
        return true;
    }

    public String describe() {
        if (isEmpty()) {
            return "No filters";
        }
        List<String> parts = new ArrayList<>();
        add(parts, "Teacher", teacher);
        add(parts, "Student group", studentGroup);
        add(parts, "Classroom", classroom);
        add(parts, "Subject", subject);
        return "Filters: " + String.join("; ", parts);
    }

    private static void add(List<String> parts, String label, String value) {
        if (!isBlank(value)) {
            parts.add(label + ": " + value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean equalsIgnoreCase(String expected, String actual) {
        return actual != null && expected.equalsIgnoreCase(actual);
    }
}
