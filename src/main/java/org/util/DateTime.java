package org.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTime {
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private DateTime() {
        // Private constructor to prevent instantiation
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now(ISTANBUL_ZONE);
    }

    public static LocalTime getCurrentTime() {
        return LocalTime.now(ISTANBUL_ZONE);
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ISTANBUL_ZONE);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    public static LocalTime parseTime(String timeStr) {
        return timeStr != null && !timeStr.isEmpty() ? LocalTime.parse(timeStr, TIME_FORMATTER) : null;
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isEmpty() ?
                LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }

    public static String getMeasurementPeriod(LocalTime time) {
        if (time == null) return "OTHER";

        int hour = time.getHour();

        if (hour >= 7 && hour < 9) {
            return "MORNING";
        } else if (hour >= 12 && hour < 14) {
            return "NOON";
        } else if (hour >= 15 && hour < 17) {
            return "AFTERNOON";
        } else if (hour >= 18 && hour < 20) {
            return "EVENING";
        } else if (hour >= 22 && hour < 24) {
            return "NIGHT";
        } else {
            return "OTHER";
        }
    }

    public static boolean isValidMeasurementTime(LocalTime time) {
        return !getMeasurementPeriod(time).equals("OTHER");
    }
}