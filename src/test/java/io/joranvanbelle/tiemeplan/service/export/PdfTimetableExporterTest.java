package io.joranvanbelle.tiemeplan.service.export;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTimetableExporterTest {

    private final PdfTimetableExporter exporter = new PdfTimetableExporter();

    @TempDir
    Path tempDir;

    @Test
    void masterPdfRendersDayColumnsTimeslotsAndAllCourseDetails() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.MASTER), Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(1);
        assertThat(reader.getPageSizeWithRotation(1).getWidth())
                .isGreaterThan(reader.getPageSizeWithRotation(1).getHeight());
        String text = text(reader);
        assertThat(text)
                .contains("Master timetable")
                .contains("MONDAY")
                .contains("TUESDAY")
                .contains("09:00")
                .contains("Mathematics")
                .contains("Jane Doe")
                .contains("Group A")
                .contains("Group B")
                .contains("Room 101");
        reader.close();
    }

    @Test
    void allTeachersProduceGridAndAgendaPagePerTeacher() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER), Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(4);
        assertThat(pageText(reader, 1)).contains("Teacher: Jane Doe").contains("Mathematics");
        assertThat(pageText(reader, 2)).contains("Agenda — Teacher: Jane Doe").contains("Mathematics");
        assertThat(pageText(reader, 3)).contains("Teacher: John Smith").contains("Physics");
        assertThat(pageText(reader, 4)).contains("Agenda — Teacher: John Smith").contains("Physics");
        reader.close();
    }

    @Test
    void allStudentGroupsDuplicateMultiGroupCourseAcrossSheets() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.STUDENT_GROUP), Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(4);
        assertThat(pageText(reader, 1)).contains("Student group: Group A").contains("Mathematics");
        assertThat(pageText(reader, 3)).contains("Student group: Group B")
                .contains("Mathematics")
                .contains("Physics");
        reader.close();
    }

    @Test
    void classroomPdfShowsBlockedFreeAndFreePeriodSummary() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.CLASSROOM), Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(4);
        String grid = pageText(reader, 1);
        assertThat(grid)
                .contains("Classroom: Room 101")
                .contains("Blocked")
                .contains("Free")
                .contains("Free periods:");
        String agenda = pageText(reader, 2);
        assertThat(agenda)
                .contains("Agenda — Classroom: Room 101")
                .contains("BLOCKED")
                .contains("FREE")
                .contains("COURSE");
        reader.close();
    }

    @Test
    void singleTeacherPdfDoesNotContainAnotherTeachersCourse() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER, ExportSelection.SINGLE,
                        "Jane Doe"),
                Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(2);
        assertThat(text(reader)).contains("Teacher: Jane Doe").doesNotContain("Physics");
        reader.close();
    }

    @Test
    void dutchLocaleUsesLocalizedDayHeaders() throws Exception {
        PdfReader reader = write(ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.MASTER), Locale.forLanguageTag("nl"));

        assertThat(text(reader)).contains("maandag").contains("dinsdag");
        reader.close();
    }

    @Test
    void manyTeachersProduceAllPagesAndNames() throws Exception {
        List<Teacher> teachers = new ArrayList<>(List.of(ExportFixture.JANE, ExportFixture.JOHN));
        teachers.add(new Teacher("Anna Adams"));
        teachers.add(new Teacher("Bob Brown"));
        teachers.add(new Teacher("Cara Clark"));
        ScenarioSnapshot snapshot = new ScenarioSnapshot(
                teachers,
                ExportFixture.snapshot().subjects(),
                ExportFixture.snapshot().studentGroups(),
                ExportFixture.snapshot().classrooms(),
                ExportFixture.snapshot().timeslots(),
                ExportFixture.courses());

        PdfReader reader = write(snapshot, ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER), Locale.ENGLISH);

        assertThat(reader.getNumberOfPages()).isEqualTo(10);
        String all = text(reader);
        assertThat(all)
                .contains("Teacher: Jane Doe")
                .contains("Teacher: John Smith")
                .contains("Teacher: Anna Adams")
                .contains("Teacher: Bob Brown")
                .contains("Teacher: Cara Clark");
        reader.close();
    }

    private PdfReader write(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request,
            Locale locale) throws IOException {
        TimetableExportDocument document = new TimetableExportModelFactory().create(snapshot, schedule, request,
                Clock.fixed(Instant.parse("2026-10-29T12:30:00Z"), ZoneOffset.UTC), locale);
        Path file = tempDir.resolve("timetable.pdf");
        exporter.write(document, file, locale);
        return new PdfReader(Files.readAllBytes(file));
    }

    private String text(PdfReader reader) throws IOException {
        StringBuilder text = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            text.append(extractor.getTextFromPage(page)).append('\n');
        }
        return text.toString();
    }

    private String pageText(PdfReader reader, int page) throws IOException {
        return new PdfTextExtractor(reader).getTextFromPage(page);
    }
}
