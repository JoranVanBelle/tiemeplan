package io.joranvanbelle.tiemeplan.service.export;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CsvTimetableExporterTest {

    private final CsvTimetableExporter exporter = new CsvTimetableExporter();

    @TempDir
    Path tempDir;

    @Test
    void courseCsvUsesExactHeaderAndOneRowPerCourseGroupPair() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.MASTER), Locale.ENGLISH);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertThat(lines.getFirst()).isEqualTo("day;timeslot;teacher;course;studentgroup;classroom");
        assertThat(lines).noneMatch(line -> line.endsWith(";"));
        assertThat(lines).containsExactly(
                "day;timeslot;teacher;course;studentgroup;classroom",
                "MONDAY;09:00;Jane Doe;Mathematics;Group A;Room 101",
                "MONDAY;09:00;Jane Doe;Mathematics;Group B;Room 101",
                "MONDAY;13:00;Jane Doe;Mathematics;Group A;Room 101",
                "TUESDAY;10:00;John Smith;Physics;Group B;Room 102");
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).doesNotContain("\r\n");
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).doesNotContain("BLOCKED");
    }

    @Test
    void allStudentGroupsExportContainsOneRowPerCourseGroupPair() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.STUDENT_GROUP), Locale.ENGLISH);

        List<String[]> rows = read(file);
        assertThat(rows).hasSize(5);
        assertThat(rows.subList(1, rows.size())).extracting(row -> row[4])
                .containsExactly("Group A", "Group B", "Group A", "Group B");
    }

    @Test
    void singleTeacherExportContainsOnlyMatchingRows() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.TEACHER, ExportSelection.SINGLE,
                        "John Smith"),
                Locale.ENGLISH);

        List<String[]> rows = read(file);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(1)).containsExactly("TUESDAY", "10:00", "John Smith", "Physics", "Group B",
                "Room 102");
    }

    @Test
    void emptyEntityStillContainsHeaderRow() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.TEACHER, ExportSelection.SINGLE,
                        "Zoe Empty"),
                Locale.ENGLISH);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertThat(lines).containsExactly("day;timeslot;teacher;course;studentgroup;classroom");
    }

    @Test
    void classroomCsvUsesOccupancySchemaAndIncludesFreeAndBlockedRows() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.CLASSROOM), Locale.ENGLISH);

        List<String[]> rows = read(file);
        assertThat(rows.getFirst()).containsExactly(
                "day", "timeslot", "classroom", "state", "teacher", "course", "studentgroup");
        assertThat(rows).hasSize(11);
        assertThat(rows.subList(1, rows.size())).extracting(row -> row[3])
                .contains("COURSE", "BLOCKED", "FREE");
        assertThat(rows.subList(1, rows.size())).extracting(row -> row[2])
                .containsOnly("Room 101", "Room 102");
        assertThat(rows.subList(1, rows.size())).extracting(row -> row[0] + "-" + row[1])
                .doesNotContain("TUESDAY-13:00");

        String[] blocked = rows.stream()
                .filter(row -> row[3].equals("BLOCKED"))
                .findFirst()
                .orElseThrow();
        assertThat(blocked).containsExactly("MONDAY", "10:00", "Room 101", "BLOCKED", "", "", "");

        String[] multiGroup = rows.stream()
                .filter(row -> row[3].equals("COURSE") && row[6].contains("Group A"))
                .findFirst()
                .orElseThrow();
        assertThat(multiGroup[6]).isEqualTo("Group A, Group B");
    }

    @Test
    void singleClassroomOccupancyContainsOnlyThatRoom() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.CLASSROOM, ExportSelection.SINGLE,
                        "Room 102"),
                Locale.ENGLISH);

        List<String[]> rows = read(file);
        assertThat(rows).hasSize(6);
        assertThat(rows.subList(1, rows.size())).extracting(row -> row[2]).containsOnly("Room 102");
    }

    @Test
    void dutchLocaleUsesLocalizedDayNames() throws Exception {
        Path file = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.CSV, ExportAudience.MASTER), Locale.forLanguageTag("nl"));

        List<String[]> rows = read(file);
        assertThat(rows.get(1)[0]).isEqualTo("maandag");
        assertThat(rows.get(4)[0]).isEqualTo("dinsdag");
    }

    @Test
    void fieldsWithSeparatorsQuotesAndUnicodeAreQuotedAndRoundTrip() throws Exception {
        var id = UUID.randomUUID().toString();
        Teacher teacher = new Teacher("Doe; Jane");
        Subject subject = new Subject("Math; Advanced");
        StudentGroup group = new StudentGroup("Group \"A\"", 20);
        Course course = new Course(id, teacher, subject, List.of(group));
        ScenarioSnapshot snapshot = new ScenarioSnapshot(
                List.of(teacher),
                List.of(subject),
                List.of(group),
                List.of(ExportFixture.ROOM_101),
                List.of(ExportFixture.MON_09),
                List.of(course));
        ClassSchedule schedule = ClassSchedule.empty()
                .assign(ExportFixture.ROOM_101, ExportFixture.MON_09, course);

        Path file = write(snapshot, schedule,
                ExportFixture.request(ExportFormat.CSV, ExportAudience.MASTER), Locale.ENGLISH);

        String raw = Files.readString(file, StandardCharsets.UTF_8);
        assertThat(raw).contains("\"Doe; Jane\"");
        assertThat(raw).contains("\"Math; Advanced\"");
        assertThat(raw).contains("\"Group \"\"A\"\"\"");

        List<String[]> rows = read(file);
        assertThat(rows.get(1)).containsExactly("MONDAY", "09:00", "Doe; Jane", "Math; Advanced",
                "Group \"A\"", "Room 101");
    }

    private Path write(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request,
            Locale locale) throws IOException {
        TimetableExportDocument document = new TimetableExportModelFactory().create(snapshot, schedule, request,
                Clock.fixed(Instant.parse("2026-10-29T12:30:00Z"), ZoneOffset.UTC), locale);
        Path file = tempDir.resolve("timetable.csv");
        exporter.write(document, file, locale);
        return file;
    }

    private List<String[]> read(Path file) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(Files.newBufferedReader(file, StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            return reader.readAll();
        }
    }
}
