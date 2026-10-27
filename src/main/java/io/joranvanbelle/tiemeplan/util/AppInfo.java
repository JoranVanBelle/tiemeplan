package io.joranvanbelle.tiemeplan.util;

import java.io.InputStream;
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
}
