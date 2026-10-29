package io.joranvanbelle.tiemeplan.service.export;

import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class ExportService {

    private final TimetableExportModelFactory modelFactory;
    private final CsvTimetableExporter csvExporter;
    private final PdfTimetableExporter pdfExporter;
    private final PngTimetableExporter pngExporter;
    private final TimetablePngSnapshotter pngSnapshotter;

    public ExportService() {
        this(new TimetableExportModelFactory(), new CsvTimetableExporter(), new PdfTimetableExporter(),
                new PngTimetableExporter(), new TimetablePngSnapshotter());
    }

    ExportService(
            TimetableExportModelFactory modelFactory,
            CsvTimetableExporter csvExporter,
            PdfTimetableExporter pdfExporter,
            PngTimetableExporter pngExporter,
            TimetablePngSnapshotter pngSnapshotter) {
        this.modelFactory = modelFactory;
        this.csvExporter = csvExporter;
        this.pdfExporter = pdfExporter;
        this.pngExporter = pngExporter;
        this.pngSnapshotter = pngSnapshotter;
    }

    public Task<ExportResult> exportTask(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request,
            Path target,
            Locale locale,
            ExportTheme currentTheme) {
        return new ExportTask(snapshot, schedule, request, target, locale, currentTheme);
    }

    private final class ExportTask extends Task<ExportResult> {

        private final ScenarioSnapshot snapshot;
        private final ClassSchedule schedule;
        private final TimetableExportRequest request;
        private final Path target;
        private final Locale locale;
        private final ExportTheme currentTheme;

        private ExportTask(
                ScenarioSnapshot snapshot,
                ClassSchedule schedule,
                TimetableExportRequest request,
                Path target,
                Locale locale,
                ExportTheme currentTheme) {
            this.snapshot = snapshot;
            this.schedule = schedule;
            this.request = request;
            this.target = target;
            this.locale = locale;
            this.currentTheme = currentTheme;
        }

        @Override
        protected ExportResult call() throws Exception {
            updateMessage("Preparing export...");
            TimetableExportDocument document = modelFactory.create(snapshot, schedule, request,
                    Clock.systemDefaultZone(), locale);
            updateProgress(0.1, 1.0);

            return switch (request.format()) {
                case CSV -> writeSingleFile(document, "CSV");
                case PDF -> writeSingleFile(document, "PDF");
                case PNG -> writePng(document);
            };
        }

        private ExportResult writeSingleFile(TimetableExportDocument document, String label) throws IOException {
            Path temp = temporaryFile(target);
            try {
                updateMessage("Writing " + label + "...");
                if (request.format() == ExportFormat.CSV) {
                    csvExporter.write(document, temp, locale);
                } else {
                    pdfExporter.write(document, temp, locale);
                }
                replace(temp, target);
                updateProgress(1.0, 1.0);
                updateMessage("Saved " + target.getFileName());
                return new ExportResult(target, document.sheets().size());
            } catch (IOException | RuntimeException e) {
                Files.deleteIfExists(temp);
                throw e;
            }
        }

        private ExportResult writePng(TimetableExportDocument document) throws Exception {
            ExportTheme theme = request.useCurrentTheme() ? currentTheme : ExportTheme.LIGHT;
            List<NamedImage> images = new ArrayList<>();
            int total = Math.max(1, document.sheets().size());
            int index = 0;
            for (TimetableSheet sheet : document.sheets()) {
                if (isCancelled()) {
                    throw new CancellationException("Export cancelled");
                }
                updateMessage("Rendering " + sheet.title() + "...");
                BufferedImage image = snapshotOnFxThread(document, sheet, theme);
                images.add(new NamedImage(directoryFor(document.audience()), sheet.entityName(), image));
                index++;
                updateProgress(0.1 + (0.8 * index / total), 1.0);
            }

            Path temp = temporaryFile(target);
            try {
                if (request.isBulkPng()) {
                    updateMessage("Packaging PNG files...");
                    pngExporter.writeZip(images, temp);
                } else {
                    updateMessage("Writing PNG...");
                    BufferedImage image = images.isEmpty() ? null : images.getFirst().image();
                    pngExporter.writeImage(image, temp);
                }
                replace(temp, target);
                updateProgress(1.0, 1.0);
                updateMessage("Saved " + target.getFileName());
                return new ExportResult(target, images.size());
            } catch (IOException | RuntimeException e) {
                Files.deleteIfExists(temp);
                throw e;
            }
        }

        private BufferedImage snapshotOnFxThread(
                TimetableExportDocument document,
                TimetableSheet sheet,
                ExportTheme theme) throws Exception {
            if (Platform.isFxApplicationThread()) {
                return pngSnapshotter.snapshot(document, sheet, locale, theme, request.pngScale());
            }
            CompletableFuture<BufferedImage> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    future.complete(pngSnapshotter.snapshot(document, sheet, locale, theme, request.pngScale()));
                } catch (Throwable error) {
                    future.completeExceptionally(error);
                }
            });
            try {
                return future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception exception) {
                    throw exception;
                }
                throw new CompletionException(cause);
            }
        }
    }

    private String directoryFor(ExportAudience audience) {
        return switch (audience) {
            case MASTER -> "master";
            case TEACHER -> "teachers";
            case STUDENT_GROUP -> "student-groups";
            case CLASSROOM -> "classrooms";
        };
    }

    private Path temporaryFile(Path target) throws IOException {
        Path absolute = target.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return Files.createTempFile(parent, absolute.getFileName().toString() + "-", ".tmp");
    }

    private void replace(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
