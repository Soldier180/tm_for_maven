package ua.sumdu.j2se.zaretsky.tasks.util;

import ua.sumdu.j2se.zaretsky.tasks.model.Task;

import java.text.ParseException;
import java.util.Date;

import static ua.sumdu.j2se.zaretsky.tasks.model.TaskIO.*;

/**
 * Class for parse tasks from string
 *
 * @author Nikolay Zaretsky
 * @version 1.0 26 Jan 2017
 */
public class TasksParser {
    /**
     * Parse Task from string
     *
     * @param stringWithTask string with one task
     * @return Task
     */
    public static Task parseTask(String stringWithTask) throws ParseException {
        Task task;
        if (stringWithTask.contains(']' + WORD_EVERY + '[')) {
            task = parseRepeatedTask(stringWithTask);
        } else {
            task = parseNotRepeatedTask(stringWithTask);
        }
        return task;
    }
    /**
     * Parse not repeated task from string
     *
     * @param stringWithTask string with one task
     * @return Task
     */
    private static Task parseNotRepeatedTask(String stringWithTask) throws ParseException {
        Task task;
        String title;
        Date time;

        title = stringWithTask.substring(1, stringWithTask.lastIndexOf(WORD_AT) - 1);
        title = parseQuotes(title);

        int timePosition = stringWithTask.lastIndexOf(WORD_AT) + WORD_AT.length();
        String timeString = stringWithTask.substring(timePosition, timePosition + TIME_PATTERN.length());
        time = DATE_F.get().parse(timeString);

        task = new Task(title, time);

        if (!stringWithTask.contains("inactive")) {
            task.setActive(true);
        }

        return task;
    }
    /**
     * Parse repeated task from string
     *
     * @param stringWithTask string with one task
     * @return Task
     */
    private static Task parseRepeatedTask(String stringWithTask) throws ParseException {
        Task task;
        String title;
        Date startTime;
        Date endTime;
        int interval;

        title = stringWithTask.substring(1, stringWithTask.lastIndexOf(WORD_FROM) - 1);
        title = parseQuotes(title);

        int startTimePosition = stringWithTask.lastIndexOf(WORD_FROM) + WORD_FROM.length();
        String startTimeString = stringWithTask.substring(startTimePosition, startTimePosition + TIME_PATTERN.length());
        startTime = DATE_F.get().parse(startTimeString);

        int endTimePos = stringWithTask.lastIndexOf(WORD_TO) + WORD_TO.length();
        String endTimeString = stringWithTask.substring(endTimePos, endTimePos + TIME_PATTERN.length());
        endTime = DATE_F.get().parse(endTimeString);

        String intervalString = stringWithTask.substring(stringWithTask.lastIndexOf('[') + 1,
                stringWithTask.lastIndexOf(']'));
        interval = DateUtil.parseInterval(intervalString);

        task = new Task(title, startTime, endTime, interval);

        if (stringWithTask.contains("inactive;") || stringWithTask.contains("inactive.")) {
            task.setActive(false);
        } else {
            task.setActive(true);
        }

        return task;
    }
    /**
     * Remove double quotes from string
     *
     * @param title string with quotes
     * @return title without quotes
     */
    private static String parseQuotes(String title) {
        return title.replaceAll("\"\"", "\"");
    }


}
