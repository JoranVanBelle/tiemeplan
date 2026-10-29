package io.joranvanbelle.tiemeplan.service.export;

public record ExportTheme(
        String background,
        String surface,
        String surfaceAlt,
        String border,
        String text,
        String secondaryText,
        String courseSurface,
        String blockedSurface,
        String freeSurface) {

    public static final ExportTheme LIGHT = new ExportTheme(
            "#f7f8fb",
            "#ffffff",
            "#eef1f8",
            "#c9cddc",
            "#151825",
            "#3f465a",
            "#e8f1ff",
            "#fee2e2",
            "#e9f9ef");

    public static final ExportTheme DARK = new ExportTheme(
            "#0b1026",
            "#151b2f",
            "#1f2540",
            "#2a3058",
            "#e7e9ee",
            "#a9b0c3",
            "#1d2d4d",
            "#3a1f28",
            "#183426");
}
