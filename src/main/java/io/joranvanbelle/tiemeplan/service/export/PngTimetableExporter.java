package io.joranvanbelle.tiemeplan.service.export;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

public final class PngTimetableExporter {

    public void writeImage(BufferedImage image, Path path) throws IOException {
        if (image == null) {
            throw new ExportException("The timetable image could not be rendered.");
        }
        boolean written = ImageIO.write(image, "png", path.toFile());
        if (!written) {
            throw new ExportException("No PNG writer is available on this system.");
        }
    }

    public void writeZip(List<NamedImage> images, Path path) throws IOException {
        try (OutputStream output = Files.newOutputStream(path);
                ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            Set<String> directories = new HashSet<>();
            Set<String> names = new HashSet<>();
            StringBuilder manifest = new StringBuilder();
            for (NamedImage image : images) {
                String directory = ExportFileNames.sanitize(image.directory());
                if (directories.add(directory)) {
                    zip.putNextEntry(new ZipEntry(directory + "/"));
                    zip.closeEntry();
                }
                String baseName = ExportFileNames.sanitize(image.name());
                String fileName = baseName + ".png";
                int index = 2;
                while (!names.add(directory + "/" + fileName)) {
                    fileName = baseName + "-" + index + ".png";
                    index++;
                }
                zip.putNextEntry(new ZipEntry(directory + "/" + fileName));
                ImageIO.write(image.image(), "png", zip);
                zip.closeEntry();
                manifest.append(directory).append('/').append(fileName).append('\n');
            }
            zip.putNextEntry(new ZipEntry("manifest.txt"));
            zip.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }
}
