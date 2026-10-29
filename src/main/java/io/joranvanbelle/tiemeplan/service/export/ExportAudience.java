package io.joranvanbelle.tiemeplan.service.export;

public enum ExportAudience {
    MASTER("Master timetable"),
    TEACHER("Teachers"),
    STUDENT_GROUP("Student groups"),
    CLASSROOM("Classrooms");

    private final String displayName;

    ExportAudience(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
