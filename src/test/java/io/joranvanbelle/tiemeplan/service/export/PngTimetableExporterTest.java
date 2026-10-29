package io.joranvanbelle.tiemeplan.service.export;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class PngTimetableExporterTest {

    private final PngTimetableExporter exporter = new PngTimetableExporter();

    @TempDir
    Path tempDir;

    @Test
    void writesAValidPngImage() throws Exception {
        BufferedImage image = image(320, 180, new Color(21, 27, 47));
        Path file = tempDir.resolve("timetable.png");

        exporter.writeImage(image, file);

        byte[] signature = new byte[8];
        try (var input = Files.newInputStream(file)) {
            assertThat(input.read(signature)).isEqualTo(8);
        }
        assertThat(signature).containsExactly((byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        BufferedImage read = ImageIO.read(file.toFile());
        assertThat(read).isNotNull();
        assertThat(read.getWidth()).isEqualTo(320);
        assertThat(read.getHeight()).isEqualTo(180);
        assertThat(new Color(read.getRGB(10, 10)).getRGB()).isEqualTo(new Color(21, 27, 47).getRGB());
    }

    @Test
    void writesZipWithDirectoryEntriesManifestAndUniqueNames() throws Exception {
        BufferedImage first = image(40, 20, Color.RED);
        BufferedImage second = image(50, 25, Color.BLUE);
        Path file = tempDir.resolve("teachers.zip");

        exporter.writeZip(List.of(
                new NamedImage("teachers", "Jane Doe", first),
                new NamedImage("teachers", "Jane/Doe", second)), file);

        try (ZipFile zip = new ZipFile(file.toFile())) {
            assertThat(zip.stream().map(ZipEntry::getName).toList())
                    .contains("teachers/", "teachers/Jane-Doe.png", "teachers/Jane-Doe-2.png",
                            "manifest.txt");
            assertThat(zip.size()).isEqualTo(4);
            ZipEntry entry = zip.getEntry("teachers/Jane-Doe.png");
            BufferedImage read = ImageIO.read(zip.getInputStream(entry));
            assertThat(read.getWidth()).isEqualTo(40);
            assertThat(read.getHeight()).isEqualTo(20);
            String manifest = new String(zip.getInputStream(zip.getEntry("manifest.txt")).readAllBytes());
            assertThat(manifest).contains("teachers/Jane-Doe.png").contains("teachers/Jane-Doe-2.png");
        }
    }

    private BufferedImage image(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
