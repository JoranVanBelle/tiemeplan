package io.joranvanbelle.tiemeplan.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class StringUtil {

    public static String label(String label) {
        return StringUtils.isBlank(label) ? "(unnamed)" : label.trim();
    }

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
