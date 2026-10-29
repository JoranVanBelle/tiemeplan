package io.joranvanbelle.tiemeplan.service.export;

public enum ExportSelection {
    ALL("All"),
    SINGLE("Specific");

    private final String displayName;

    ExportSelection(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
