package io.joranvanbelle.tiemeplan.service.export;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportFileNamesTest {

    private static final Instant NOW = Instant.parse("2026-10-29T14:30:00Z");

    @Test
    void suggestsCombinedPdfName() {
        TimetableExportRequest request = ExportFixture.request(ExportFormat.PDF, ExportAudience.TEACHER);
        assertThat(ExportFileNames.suggest(request, NOW, ZoneOffset.UTC))
                .isEqualTo("timetable-teachers-2026-10-29-1430.pdf");
    }

    @Test
    void suggestsZipForBulkPngAndPngForSingleEntity() {
        TimetableExportRequest all = ExportFixture.request(ExportFormat.PNG, ExportAudience.CLASSROOM);
        assertThat(ExportFileNames.suggest(all, NOW, ZoneOffset.UTC))
                .isEqualTo("timetable-classrooms-2026-10-29-1430.zip");

        TimetableExportRequest single = ExportFixture.request(ExportFormat.PNG, ExportAudience.TEACHER,
                ExportSelection.SINGLE, "Jane Doe");
        assertThat(ExportFileNames.suggest(single, NOW, ZoneOffset.UTC))
                .isEqualTo("timetable-teachers-Jane-Doe-2026-10-29-1430.png");
    }

    @Test
    void masterPngIsSingleFile() {
        TimetableExportRequest request = ExportFixture.request(ExportFormat.PNG, ExportAudience.MASTER);
        assertThat(request.isBulkPng()).isFalse();
        assertThat(ExportFileNames.extension(request)).isEqualTo(".png");
    }

    @Test
    void ensuresExtensionAndSanitizesNames() {
        assertThat(ExportFileNames.ensureExtension(Path.of("timetable.pdf"), ".pdf"))
                .isEqualTo(Path.of("timetable.pdf"));
        assertThat(ExportFileNames.ensureExtension(Path.of("timetable"), ".pdf"))
                .isEqualTo(Path.of("timetable.pdf"));
        assertThat(ExportFileNames.sanitize("  Jane / Doe  ")).isEqualTo("Jane-Doe");
        assertThat(ExportFileNames.sanitize("...")).isEqualTo("...");
        assertThat(ExportFileNames.sanitize("   ")).isEqualTo("entity");
    }
}
