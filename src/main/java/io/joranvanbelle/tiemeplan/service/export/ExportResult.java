package io.joranvanbelle.tiemeplan.service.export;

import java.nio.file.Path;

public record ExportResult(Path path, int fileCount) {
}
