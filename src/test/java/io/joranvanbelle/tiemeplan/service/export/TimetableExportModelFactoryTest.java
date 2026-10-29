package io.joranvanbelle.tiemeplan.service.export;

import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimetableExportModelFactoryTest {

    private final TimetableExportModelFactory factory = new TimetableExportModelFactory();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-29T12:30:00Z"), ZoneOffset.UTC);

    @Test
    void masterSheetUsesDayColumnsAndScenarioTimeslotRows() {
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.MASTER));

        TimetableSheet sheet = document.sheets().getFirst();
        assertThat(sheet.days()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        assertThat(sheet.rowHeaders()).extracting(RowHeader::label).containsExactly("09:00", "10:00", "13:00");
        assertThat(sheet.cell(0, 0).state()).isEqualTo(SlotState.COURSE);
        assertThat(sheet.cell(0, 0).entries()).singleElement().satisfies(entry -> {
            assertThat(entry.subject()).isEqualTo("Mathematics");
            assertThat(entry.teacher()).isEqualTo("Jane Doe");
            assertThat(entry.classroom()).isEqualTo("Room 101");
            assertThat(entry.studentGroups()).containsExactly("Group A", "Group B");
        });
        assertThat(sheet.cell(0, 1).state()).isEqualTo(SlotState.FREE);
        assertThat(sheet.cell(2, 1).state()).isEqualTo(SlotState.NOT_A_TIMESLOT);
    }

    @Test
    void allTeachersIncludeEmptyTeachers() {
        ScenarioSnapshot snapshot = new ScenarioSnapshot(
                List.of(ExportFixture.JANE, ExportFixture.JOHN, new Teacher("Zoe Empty")),
                ExportFixture.snapshot().subjects(),
                ExportFixture.snapshot().studentGroups(),
                ExportFixture.snapshot().classrooms(),
                ExportFixture.snapshot().timeslots(),
                ExportFixture.courses());

        TimetableExportDocument document = create(snapshot, ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER));

        assertThat(document.sheets()).extracting(TimetableSheet::entityName)
                .containsExactly("Jane Doe", "John Smith", "Zoe Empty");
        TimetableSheet empty = document.sheets().getLast();
        assertThat(empty.scheduledCourseCount()).isZero();
        assertThat(empty.subtitle()).isEqualTo("No scheduled courses");
    }

    @Test
    void studentGroupSheetsDuplicateMultiGroupCourse() {
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.STUDENT_GROUP));

        assertThat(document.sheets()).extracting(TimetableSheet::entityName)
                .containsExactly("Group A", "Group B");
        for (TimetableSheet sheet : document.sheets()) {
            assertThat(sheet.cell(0, 0).state()).isEqualTo(SlotState.COURSE);
            assertThat(sheet.cell(0, 0).entries()).singleElement()
                    .extracting(TimetableCellEntry::subject)
                    .isEqualTo("Mathematics");
        }
    }

    @Test
    void classroomSheetsDistinguishCourseBlockedFreeAndNotATimeslot() {
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.CLASSROOM));

        TimetableSheet room101 = document.sheets().stream()
                .filter(sheet -> "Room 101".equals(sheet.entityName()))
                .findFirst()
                .orElseThrow();
        assertThat(room101.cell(0, 0).state()).isEqualTo(SlotState.COURSE);
        assertThat(room101.cell(1, 0).state()).isEqualTo(SlotState.BLOCKED);
        assertThat(room101.cell(0, 1).state()).isEqualTo(SlotState.FREE);
        assertThat(room101.cell(2, 1).state()).isEqualTo(SlotState.NOT_A_TIMESLOT);
        assertThat(room101.notes()).anyMatch(note -> note.startsWith("Free periods:"));
        assertThat(room101.notes()).contains("Legend: Course, Blocked, Free, No period.");
        assertThat(room101.subtitle()).contains("blocked periods").contains("free periods");
    }

    @Test
    void filtersApplyToMasterContent() {
        ExportFilters filters = new ExportFilters("John Smith", null, null, null);
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.requestWithFilters(ExportFormat.PDF, ExportAudience.MASTER,
                        ExportSelection.ALL, null, filters));

        TimetableSheet sheet = document.sheets().getFirst();
        assertThat(sheet.cell(0, 0).state()).isEqualTo(SlotState.FREE);
        assertThat(sheet.cell(1, 1).entries()).singleElement()
                .extracting(TimetableCellEntry::subject)
                .isEqualTo("Physics");
        assertThat(document.filterSummary()).contains("Teacher: John Smith");
    }

    @Test
    void allTeacherExportIgnoresMatchingTeacherFilter() {
        ExportFilters filters = new ExportFilters("John Smith", null, null, null);
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.requestWithFilters(ExportFormat.PDF, ExportAudience.TEACHER,
                        ExportSelection.ALL, null, filters));

        TimetableSheet jane = document.sheets().stream()
                .filter(sheet -> "Jane Doe".equals(sheet.entityName()))
                .findFirst()
                .orElseThrow();
        assertThat(jane.cell(0, 0).state()).isEqualTo(SlotState.COURSE);
        assertThat(document.filterSummary()).isEqualTo("No filters");
    }

    @Test
    void conflictsAreSurfacedInsteadOfDropped() {
        Course first = new Course(ExportFixture.JANE, ExportFixture.MATHEMATICS, List.of(ExportFixture.GROUP_A));
        Course second = new Course(ExportFixture.JANE, ExportFixture.PHYSICS, List.of(ExportFixture.GROUP_B));
        ClassSchedule schedule = ClassSchedule.empty()
                .assign(ExportFixture.ROOM_101, ExportFixture.MON_09, first)
                .assign(ExportFixture.ROOM_102, ExportFixture.MON_09, second);

        TimetableExportDocument document = create(ExportFixture.snapshot(), schedule,
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER, ExportSelection.SINGLE,
                        "Jane Doe"));

        TimetableSheet sheet = document.sheets().getFirst();
        assertThat(sheet.cell(0, 0).state()).isEqualTo(SlotState.CONFLICT);
        assertThat(sheet.cell(0, 0).entries()).extracting(TimetableCellEntry::subject)
                .containsExactly("Mathematics", "Physics");
    }

    @Test
    void singleEntityExportContainsOnlyRequestedEntity() {
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER, ExportSelection.SINGLE,
                        "John Smith"));

        assertThat(document.sheets()).singleElement()
                .satisfies(sheet -> {
                    assertThat(sheet.entityName()).isEqualTo("John Smith");
                    assertThat(sheet.title()).isEqualTo("Teacher: John Smith");
                });
    }

    @Test
    void classroomFilterAppliesToMasterContent() {
        ExportFilters filters = new ExportFilters(null, null, "Room 102", null);
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.requestWithFilters(ExportFormat.PDF, ExportAudience.MASTER,
                        ExportSelection.ALL, null, filters));

        TimetableSheet sheet = document.sheets().getFirst();
        assertThat(sheet.cell(0, 0).state()).isEqualTo(SlotState.FREE);
        assertThat(sheet.cell(1, 1).entries()).singleElement()
                .extracting(TimetableCellEntry::classroom)
                .isEqualTo("Room 102");
    }

    @Test
    void generatedTimestampComesFromTheInjectedClock() {
        TimetableExportDocument document = create(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.MASTER));

        assertThat(document.generatedAt()).isEqualTo(Instant.parse("2026-10-29T12:30:00Z"));
    }

    private TimetableExportDocument create(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request) {
        return factory.create(snapshot, schedule, request, clock, Locale.ENGLISH);
    }
}
