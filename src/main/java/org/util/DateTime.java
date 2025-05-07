package util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTime {
    public static LocalDateTime parseTimestamp(String ts) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return LocalDateTime.parse(ts, fmt);
    }

    public static String format(LocalDateTime dt) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dt.format(fmt);
    }
}
