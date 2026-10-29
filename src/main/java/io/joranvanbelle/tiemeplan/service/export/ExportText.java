package io.joranvanbelle.tiemeplan.service.export;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;

public final class ExportText {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private ExportText() {
    }

    public static String dayName(DayOfWeek day, Locale locale) {
        if (day == null) {
            return "";
        }
        if ("en".equalsIgnoreCase(locale.getLanguage())) {
            return day.name();
        }
        return day.getDisplayName(TextStyle.FULL, locale);
    }

    public static String shortDayName(DayOfWeek day, Locale locale) {
        if (day == null) {
            return "";
        }
        return day.getDisplayName(TextStyle.SHORT, locale);
    }

    public static String time(LocalTime time) {
        return time == null ? "" : TIME_FORMAT.format(time);
    }

    public static String timestamp(Instant instant, Locale locale, ZoneId zoneId) {
        if (instant == null) {
            return "";
        }
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(zoneId)
                .format(instant);
    }
}
