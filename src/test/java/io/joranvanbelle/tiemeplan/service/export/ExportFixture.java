package io.joranvanbelle.tiemeplan.service.export;

import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class ExportFixture {

    public static final Teacher JANE = new Teacher("Jane Doe");
    public static final Teacher JOHN = new Teacher("John Smith");
    public static final Subject MATHEMATICS = new Subject("Mathematics");
    public static final Subject PHYSICS = new Subject("Physics");
    public static final StudentGroup GROUP_A = new StudentGroup("Group A", 20);
    public static final StudentGroup GROUP_B = new StudentGroup("Group B", 25);
    public static final Classroom ROOM_101 = new Classroom("Room 101", 30,
            List.of(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0))));
    public static final Classroom ROOM_102 = new Classroom("Room 102", 25, List.of());

    public static final Timeslot MON_09 = new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 0));
    public static final Timeslot MON_10 = new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 0));
    public static final Timeslot MON_13 = new Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 0));
    public static final Timeslot TUE_09 = new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 0));
    public static final Timeslot TUE_10 = new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 0));

    private ExportFixture() {
    }

    public static ScenarioSnapshot snapshot() {
        return new ScenarioSnapshot(
                List.of(JANE, JOHN),
                List.of(MATHEMATICS, PHYSICS),
                List.of(GROUP_A, GROUP_B),
                List.of(ROOM_101, ROOM_102),
                List.of(MON_09, MON_10, MON_13, TUE_09, TUE_10),
                courses());
    }

    public static List<Course> courses() {
        return List.of(
                new Course(UUID.randomUUID().toString(), JANE, MATHEMATICS, List.of(GROUP_A, GROUP_B), null),
                new Course(UUID.randomUUID().toString(), JOHN, PHYSICS, List.of(GROUP_B), null),
                new Course(UUID.randomUUID().toString(), JANE, MATHEMATICS, List.of(GROUP_A), null));
    }

    public static ClassSchedule schedule() {
        return ClassSchedule.empty()
                .addUnavailability(List.of(ROOM_101, ROOM_102))
                .assign(ROOM_101, MON_09, new Course(UUID.randomUUID().toString(), JANE, MATHEMATICS, List.of(GROUP_A, GROUP_B), null))
                .assign(ROOM_102, TUE_10, new Course(UUID.randomUUID().toString(), JOHN, PHYSICS, List.of(GROUP_B), null))
                .assign(ROOM_101, MON_13, new Course(UUID.randomUUID().toString(), JANE, MATHEMATICS, List.of(GROUP_A), null));
    }

    public static TimetableExportRequest request(ExportFormat format, ExportAudience audience) {
        return new TimetableExportRequest(format, audience, ExportSelection.ALL, null,
                ExportFilters.none(), false, 2.0, true);
    }

    public static TimetableExportRequest requestWithFilters(
            ExportFormat format,
            ExportAudience audience,
            ExportSelection selection,
            String entity,
            ExportFilters filters) {
        return new TimetableExportRequest(format, audience, selection, entity,
                filters, true, 2.0, true);
    }

    public static TimetableExportRequest request(
            ExportFormat format,
            ExportAudience audience,
            ExportSelection selection,
            String entity) {
        return new TimetableExportRequest(format, audience, selection, entity,
                ExportFilters.none(), false, 2.0, true);
    }

    public static TimetableExportDocument document(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request) {
        return new TimetableExportModelFactory().create(snapshot, schedule, request,
                java.time.Clock.fixed(java.time.Instant.parse("2026-10-29T12:30:00Z"), java.time.ZoneOffset.UTC),
                java.util.Locale.ENGLISH);
    }
}
