package ua.sumdu.j2se.zaretsky.tasks.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * Class with helper functions for working with dates.
 */
public class DateUtil {

    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    private static final ThreadLocal<DateFormat> DATE_F
            = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(TIME_PATTERN, Locale.ENGLISH);
        }
    };


    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_F.get().format(date);
    }

    /**
     * Convert time from seconds in a readable string format
     *
     * @param time - time into seconds
     * @return String, time in a readable format.
     */
    public static String secondsToStringTime(int time) {
        String result = "";
        int days = time / (60 * 60 * 24);
        int hours = (time - days * 60 * 60 * 24) / (60 * 60);
        int minutes = (time - (days * 60 * 60 * 24) - (hours * 60 * 60)) / 60;
        int seconds = time - (days * 60 * 60 * 24) - (hours * 60 * 60) - (minutes * 60);

        if (days > 0) {
            result = result + days + ending(days, " day");
        }
        if (hours > 0) {
            result = result + " " + hours + ending(hours, " hour");
        }
        if (minutes > 0) {
            result = result + " " + minutes + ending(minutes, " minute");
        }
        if (seconds > 0) {
            result = result + " " + seconds + ending(seconds, " second");
        }
        result = result.trim();
        return result;
    }

    private static String ending(int time, String word) {
        if (time < 2) {
            return word;
        } else {
            return word + "s";
        }
    }

    /**
     * Convert time from string in (integer) seconds
     *
     * @param intervalString - time into string
     * @return int, time into seconds.
     */
    public static int parseInterval(String intervalString) throws IllegalArgumentException {
        if (intervalString == null || intervalString.isEmpty()) {
            return 0;
        }
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (!validateTitle(intervalString)) {
            throw new IllegalArgumentException("Interval does not contain the keywords: day, " +
                    "hour, minute or second");
        }
        String[] parts = intervalString.split(" ");


        if (parts.length < 2) {
            throw new IllegalArgumentException("Interval must contains of " +
                    "number and word");
        }
        for (int i = 0; i < parts.length; i = i + 2) {
            String time = parts[i + 1];
            int value = Integer.parseInt(parts[i]);

            day = parseKeyWord(time, DAY, value);
            hour = parseKeyWord(time, HOUR, value);
            minute = parseKeyWord(time, MINUTE, value);
            second = parseKeyWord(time, SECOND, value);
        }
        if (validateTime(day, hour, minute, second)) {
            throw new IllegalArgumentException("Incorrect time");
        }
        return ((day * 60 * 60 * 24) + (hour * 60 * 60) + (minute * 60) + second);
    }

    private static int parseKeyWord(String timeWord, String keyWord, int value) {
        if (timeWord.contains(keyWord)) {
            return value;
        } else return 0;
    }

    private static boolean validateTime(int day, int hour, int minute, int second) {
        return day < 0 || hour < 0 || minute < 0 || second < 0;
    }

    private static boolean validateTitle(String intervalString) {
        return intervalString.contains(DAY) || intervalString.contains(HOUR) || intervalString
                .contains(MINUTE) || intervalString.contains(SECOND);
    }

    public static String choiceBoxTime(int timeString) {
        String time = "";
        if (timeString >= 0 && timeString < 10) {
            time = "0" + Integer.toString(timeString);
            return time;
        } else {
            time = Integer.toString(timeString);
            return time;
        }
    }

    /**
     * Convert date from type Date into LocalDate.
     *
     * @param date - date in type Date
     * @return LocalDate
     */
    public static LocalDate dateToLaocalDate(Date date) {
        LocalDate result;
        result = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return result;
    }

    /**
     * Convert date from type LocalDate into Date.
     *
     * @param localDate - date in type Date
     * @return Date
     */
    public static Date localDateToDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }

}