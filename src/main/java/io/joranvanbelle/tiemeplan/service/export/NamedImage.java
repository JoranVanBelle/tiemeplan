package io.joranvanbelle.tiemeplan.service.export;

import java.awt.image.BufferedImage;

public record NamedImage(String directory, String name, BufferedImage image) {
}
