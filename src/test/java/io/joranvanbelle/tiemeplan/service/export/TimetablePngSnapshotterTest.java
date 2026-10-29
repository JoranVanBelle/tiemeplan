package io.joranvanbelle.tiemeplan.service.export;

import java.awt.image.BufferedImage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimetablePngSnapshotterTest {

    @BeforeAll
    static void startToolkit() throws Exception {
        Assumptions.assumeTrue(System.getenv("DISPLAY") != null, "JavaFX needs a display");
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();
        } catch (Throwable unavailable) {
            Assumptions.assumeTrue(false, "JavaFX toolkit unavailable: " + unavailable);
        }
        Assumptions.assumeTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    @Test
    void snapshotContainsRenderedContent() throws Exception {
        TimetableExportDocument document = new TimetableExportModelFactory().create(
                ExportFixture.snapshot(), ExportFixture.schedule(),
                ExportFixture.request(ExportFormat.PNG, ExportAudience.MASTER),
                Clock.fixed(Instant.parse("2026-10-29T12:30:00Z"), ZoneOffset.UTC), Locale.ENGLISH);

        AtomicReference<BufferedImage> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(new TimetablePngSnapshotter().snapshot(
                        document, document.sheets().getFirst(), Locale.ENGLISH, ExportTheme.LIGHT, 1.0));
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                done.countDown();
            }
        });
        assertThat(done.await(20, TimeUnit.SECONDS)).isTrue();
        assertThat(failure.get()).isNull();
        BufferedImage image = result.get();
        assertThat(image.getWidth()).isGreaterThan(100);
        assertThat(image.getHeight()).isGreaterThan(100);

        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 2) {
            for (int x = 0; x < image.getWidth(); x += 2) {
                colors.add(image.getRGB(x, y));
            }
        }
        System.out.println("PNG size=" + image.getWidth() + "x" + image.getHeight()
                + " colors=" + colors.size());
        assertThat(colors).hasSizeGreaterThan(5);
    }
}
