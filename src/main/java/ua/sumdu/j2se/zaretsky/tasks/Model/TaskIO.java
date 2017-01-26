package ua.sumdu.j2se.zaretsky.tasks.model;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Nikolion on 27.11.2016.
 */
public class TaskIO {

    private static final String TO = " to ";
    private static final String FROM = " from ";
    private static final String AT = " at ";
    private static final String EVERY = " every ";

    private static final String TIME_PATTERN = "[yyyy-MM-dd HH:mm:ss.sss]";
    //private static final SimpleDateFormat DATE_F = new SimpleDateFormat(TIME_PATTERN);

    private static final ThreadLocal<DateFormat> DATE_F
            = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(TIME_PATTERN, Locale.ENGLISH);
        }
    };


    public static void write(TaskList tasks, OutputStream out) throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(out);
            oos.writeInt(tasks.size());
            for (Task task : (Iterable<Task>) tasks) {
                oos.writeInt(task.getTitle().length());
                oos.writeObject(task.getTitle());
                oos.writeInt(task.isActive() ? 1 : 0);
                oos.writeInt(task.getRepeatInterval());
                if (task.isRepeated()) {
                    oos.writeLong(task.getStartTime().getTime());
                    oos.writeLong(task.getEndTime().getTime());
                } else {
                    oos.writeLong(task.getTime().getTime());
                }
            }
        } finally {
            oos.flush();
            oos.close();
        }


    }

    public static void read(TaskList tasks, InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            int a = ois.readInt();
            for (int i = 0; i < a; i++) {
                int lengthTitle = ois.readInt();
                String title = (String) ois.readObject();
                boolean active = ois.readInt() == 1;
                int repeat = ois.readInt();
                if (repeat == 0) {
                    long startTime = ois.readLong();
                    Task task = new Task(title, new Date(startTime));
                    task.setActive(active);
                    tasks.add(task);
                } else {
                    long startTime = ois.readLong();
                    long endTime = ois.readLong();
                    Task task = new Task(title, new Date(startTime), new Date
                            (endTime), repeat);
                    task.setActive(active);
                    tasks.add(task);
                }
            }
        } finally {
            ois.close();
        }


    }

    public static void writeBinary(TaskList tasks, File file) throws
            IOException {
        FileOutputStream fileOutputStr = new FileOutputStream(file);
        try {
            write(tasks, fileOutputStr);
        } finally {
            fileOutputStr.flush();
            fileOutputStr.close();
        }
    }

    public static void readBinary(TaskList tasks, File file) throws IOException,
            ClassNotFoundException {

        FileInputStream fis = new FileInputStream(file);
        read(tasks, fis);
        fis.close();


    }

    public static void write(TaskList tasks, Writer out) throws IOException {
        try {

            int numLine = 1;

            for (Task t:tasks) {
                out.write(getTitleModif(t.getTitle()));
                if (!t.isActive() && !t.isRepeated()) {
                    out.write(AT + DATE_F.get().format
                            (t.getStartTime()) + " is inactive");

                } else if (!t.isActive() && t.isRepeated()) {
                    out.write(FROM + DATE_F.get().format
                            (t.getStartTime())
                            + TO + DATE_F.get().format(t.getEndTime())
                            + EVERY + secondsToStringTime(t.getRepeatInterval
                            ()) + " is " +
                            "inactive");
                } else if (t.isActive() && t.isRepeated()) {
                    out.write(FROM + DATE_F.get().format
                            (t.getStartTime())
                            + TO + DATE_F.get().format(t.getEndTime())
                            + EVERY + secondsToStringTime(t.getRepeatInterval
                            ()));
                } else if (t.isActive() && !t.isRepeated()) {
                    out.write(AT + DATE_F.get().format
                            (t.getStartTime()));
                }
                if (numLine < tasks.count) {
                    out.write(";");
                } else {
                    out.write(".");
                }
                out.write(System.getProperty("line.separator"));
                numLine++;
            }
        } finally {
            out.flush();
            out.close();
        }


    }

    public static void read(TaskList tasks, Reader in) throws IOException, ParseException {
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(in);
            String taskString;

            while ((taskString = bufferedReader.readLine()) != null) {
                Task task = parseTask(taskString);
                tasks.add(task);
            }
        } finally {
            bufferedReader.close();
        }

    }


    public static void writeText(TaskList tasks, File file) throws
            IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            write(tasks, writer);
        } finally {
            //writer.flush();
            writer.close();
        }

    }

    public static void readText(TaskList tasks, File file) throws IOException, ParseException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            read(tasks, fileReader);
        } finally {
            fileReader.close();
        }
    }


    public static String secondsToStringTime(int timeSeconds) {
        String result = "";
        int days = timeSeconds / (60 * 60 * 24);
        int hours = (timeSeconds - days * 60 * 60 * 24) / (60 * 60);
        int minutes = (timeSeconds - (days * 60 * 60 * 24) - (hours * 60 * 60)) / 60;
        int seconds = timeSeconds - (days * 60 * 60 * 24) - (hours * 60 * 60) - (minutes * 60);

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
        return "[" + result + "]";
    }

    private static String ending(int time, String wordWithEnding) {
        if (time < 2) {
            return wordWithEnding;
        } else {
            return wordWithEnding + "s";
        }
    }

    private static String getTitleModif(String title) {
        return "\"" + title.replaceAll("\"", "\"\"") + "\"";
    }

    private static Task parseTask(String stringWithTask) throws ParseException {
        Task task;
        if (stringWithTask.contains("] every [")) {
            task = parseRepeatedTask(stringWithTask);
        } else {
            task = parseNotRepeatedTask(stringWithTask);
        }
        return task;
    }


    private static Task parseNotRepeatedTask(String stringWithTask) throws ParseException {
        Task task;
        String title;
        Date time;

        title = stringWithTask.substring(1, stringWithTask.lastIndexOf(AT) - 1);
        title = parseQuotes(title);

        int timePosition = stringWithTask.lastIndexOf(AT) + AT.length();
        String timeString = stringWithTask.substring(timePosition, timePosition + TIME_PATTERN.length());
        time = DATE_F.get().parse(timeString);

        task = new Task(title, time);

        if (!stringWithTask.contains("inactive")) {
            task.setActive(true);
        }

        return task;
    }

    private static Task parseRepeatedTask(String stringWithTask) throws ParseException {
        Task task;
        String title;
        Date startTime;
        Date endTime;
        int interval;

        title = stringWithTask.substring(1, stringWithTask.lastIndexOf(FROM) - 1);
        title = parseQuotes(title);

        int startTimePosition = stringWithTask.lastIndexOf(FROM) + FROM.length();
        String startTimeString = stringWithTask.substring(startTimePosition, startTimePosition + TIME_PATTERN.length());
        startTime = DATE_F.get().parse(startTimeString);

        int endTimePos = stringWithTask.lastIndexOf(TO) + TO.length();
        String endTimeString = stringWithTask.substring(endTimePos, endTimePos + TIME_PATTERN.length());
        endTime = DATE_F.get().parse(endTimeString);

        String intervalString = stringWithTask.substring(stringWithTask.lastIndexOf('[') + 1,
                stringWithTask.lastIndexOf(']'));
        interval = parseInterval(intervalString);

        task = new Task(title, startTime, endTime, interval);

        if (!stringWithTask.contains("inactive;")) {
            task.setActive(true);
        }

        return task;
    }

    private static String parseQuotes(String title) {
        return title.replaceAll("\"\"", "\"");
    }


    private static int parseInterval(String intervalString) throws
            ParseException, IllegalArgumentException {
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        String[] parts = intervalString.split(" ");
        for (int i = 0; i < parts.length; i = i + 2) {
            if (parts[i + 1].contains("day")) {
                day = Integer.parseInt(parts[i]);
                continue;
            }
            if (parts[i + 1].contains("hour")) {
                hour = Integer.parseInt(parts[i]);
                continue;
            }
            if (parts[i + 1].contains("minute")) {
                minute = Integer.parseInt(parts[i]);
                continue;
            }
            if (parts[i + 1].contains("second")) {
                second = Integer.parseInt(parts[i]);
            }
        }
        if (day < 0 || hour < 0 || minute < 0 || second < 0 || hour >= 24 ||
                minute > 59 || second > 59) {
            throw new IllegalArgumentException("Incorrect time");
        }
        return ((day * 60 * 60 * 24) + (hour * 60 * 60) + (minute * 60) + second);
    }

    public static String writeTask(Task task){
        String result = "";

        result= result.concat(getTitleModif(task.getTitle()));
        if (!task.isActive() && !task.isRepeated()) {
           result= result.concat(AT + DATE_F.get().format
                    (task.getStartTime()) + " is inactive");

        } else if (!task.isActive() && task.isRepeated()) {
            result=result.concat(FROM + DATE_F.get().format
                    (task.getStartTime())
                    + TO + DATE_F.get().format(task.getEndTime())
                    + EVERY + secondsToStringTime(task.getRepeatInterval
                    ()) + " is " +
                    "inactive");
        } else if (task.isActive() && task.isRepeated()) {
            result=result.concat(FROM + DATE_F.get().format
                    (task.getStartTime())
                    + TO + DATE_F.get().format(task.getEndTime())
                    + EVERY + secondsToStringTime(task.getRepeatInterval
                    ()));
        } else if (task.isActive() && !task.isRepeated()) {
            result=result.concat(AT + DATE_F.get().format
                    (task.getStartTime()));
        }

        return result;
    }


}
