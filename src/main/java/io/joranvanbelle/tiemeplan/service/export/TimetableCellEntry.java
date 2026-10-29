package io.joranvanbelle.tiemeplan.service.export;

import java.util.List;

public record TimetableCellEntry(
        String subject,
        String teacher,
        String classroom,
        List<String> studentGroups,
        int participantCount) {

    public TimetableCellEntry {
        studentGroups = studentGroups == null ? List.of() : List.copyOf(studentGroups);
    }

    public String studentGroupsLabel() {
        return String.join(", ", studentGroups);
    }
}
