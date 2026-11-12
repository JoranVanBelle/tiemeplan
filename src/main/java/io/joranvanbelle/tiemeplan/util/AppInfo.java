package io.joranvanbelle.tiemeplan.util;

import java.io.InputStream;
import java.time.Year;
import java.util.Properties;

public final class AppInfo {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppInfo.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception ignored) {
        }
    }

    private AppInfo() {
    }

    public static String getVersion() {
        return PROPS.getProperty("app.version", "dev");
    }

    public static String getName() {
        return PROPS.getProperty("app.name", "Tiemeplan");
    }

    public static String getDescription() {
        return PROPS.getProperty("app.description", "Create time plans for schools using constraints");
    }

    public static String getUrl() {
        return PROPS.getProperty("app.url", "");
    }

    public static String getIssuesUrl() {
        return PROPS.getProperty("app.issues.url", "");
    }

    public static String getVendor() {
        return PROPS.getProperty("app.vendor", "");
    }

    public static String getLicense() {
        return PROPS.getProperty("app.license", "");
    }

    public static String getLicenseUrl() {
        return PROPS.getProperty("app.license.url", "");
    }

    public static int getJavaVersion() {
        return Runtime.version().feature();
    }

    public static String getJavaFxVersion() {
        return System.getProperty("javafx.version", "unknown");
    }

    public static String getPlatform() {
        return System.getProperty("os.name", "unknown") + " (" + System.getProperty("os.arch", "unknown") + ")";
    }

    public static int getCopyrightYear() {
        return Year.now().getValue();
    }

    public static String diagnostics() {
        return getName() + " " + getVersion() + System.lineSeparator() + "Java " + getJavaVersion()
                + System.lineSeparator() + "JavaFX " + getJavaFxVersion() + System.lineSeparator() + "Platform "
                + getPlatform();
    }
}
