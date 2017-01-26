package ua.sumdu.j2se.zaretsky.tasks.model;

import ua.sumdu.j2se.zaretsky.tasks.util.DateUtil;
import ua.sumdu.j2se.zaretsky.tasks.util.TasksParser;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class for reading and writing tasks from stream.
 */
public class TaskIO {

    public static final String TO = " to ";
    public static final String FROM = " from ";
    public static final String AT = " at ";
    public static final String EVERY = " every ";

    public static final String TIME_PATTERN = "[yyyy-MM-dd HH:mm:ss.sss]";
    //private static final SimpleDateFormat DATE_F = new SimpleDateFormat(TIME_PATTERN);

    public static final ThreadLocal<DateFormat> DATE_F
            = new ThreadLocal<DateFormat>() {
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
            for (Task task : tasks) {
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
            assert oos != null;
            //noinspection ThrowFromFinallyBlock
            oos.flush();
            //noinspection ThrowFromFinallyBlock
            oos.close();
        }


    }

    public static void read(TaskList tasks, InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            int tasksCount = ois.readInt();
            for (int i = 0; i < tasksCount; i++) {
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
            assert ois != null;
            //noinspection ThrowFromFinallyBlock
            ois.close();
        }


    }

    public static void writeBinary(TaskList tasks, File file) throws
            IOException {
        FileOutputStream fileOutputStr = new FileOutputStream(file);
        try {
            write(tasks, fileOutputStr);
        } finally {
            //noinspection ThrowFromFinallyBlock
            fileOutputStr.flush();
            //noinspection ThrowFromFinallyBlock
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

            for (Task task : tasks) {
                out.write(getTitleModification(task.getTitle()));
                if (!task.isActive() && !task.isRepeated()) {
                    out.write(AT + DATE_F.get().format
                            (task.getStartTime()) + " is inactive");

                } else if (!task.isActive() && task.isRepeated()) {
                    out.write(FROM + DATE_F.get().format
                            (task.getStartTime())
                            + TO + DATE_F.get().format(task.getEndTime())
                            + EVERY + secondsToStringTime(task.getRepeatInterval
                            ()) + " is " +
                            "inactive");
                } else if (task.isActive() && task.isRepeated()) {
                    out.write(FROM + DATE_F.get().format
                            (task.getStartTime())
                            + TO + DATE_F.get().format(task.getEndTime())
                            + EVERY + secondsToStringTime(task.getRepeatInterval
                            ()));
                } else if (task.isActive() && !task.isRepeated()) {
                    out.write(AT + DATE_F.get().format
                            (task.getStartTime()));
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
            //noinspection ThrowFromFinallyBlock
            out.flush();
            //noinspection ThrowFromFinallyBlock
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
            assert bufferedReader != null;
            //noinspection ThrowFromFinallyBlock
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
            assert writer != null;
            //noinspection ThrowFromFinallyBlock
            writer.close();
        }

    }

    public static void readText(TaskList tasks, File file) throws IOException, ParseException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            read(tasks, fileReader);
        } finally {
            assert fileReader != null;
            //noinspection ThrowFromFinallyBlock
            fileReader.close();
        }
    }


    private static String secondsToStringTime(int timeSeconds) {
        String result;
        result = DateUtil.secondsToStringTime(timeSeconds);
        result = result.trim();
        return "[" + result + "]";
    }


    private static String getTitleModification(String title) {
        return "\"" + title.replaceAll("\"", "\"\"") + "\"";
    }

    private static Task parseTask(String stringWithTask) throws ParseException {
        Task task;
        if (stringWithTask.contains(']' + EVERY + '[')) {
            task = TasksParser.parseRepeatedTask(stringWithTask);
        } else {
            task = TasksParser.parseNotRepeatedTask(stringWithTask);
        }
        return task;
    }




    public static String writeTask(Task task) {
        String result = "";

        result = result.concat(getTitleModification(task.getTitle()));
        if (!task.isActive() && !task.isRepeated()) {
            result = result.concat(AT + DATE_F.get().format
                    (task.getStartTime()) + " is inactive");

        } else if (!task.isActive() && task.isRepeated()) {
            result = result.concat(FROM + DATE_F.get().format
                    (task.getStartTime())
                    + TO + DATE_F.get().format(task.getEndTime())
                    + EVERY + secondsToStringTime(task.getRepeatInterval
                    ()) + " is " +
                    "inactive");
        } else if (task.isActive() && task.isRepeated()) {
            result = result.concat(FROM + DATE_F.get().format
                    (task.getStartTime())
                    + TO + DATE_F.get().format(task.getEndTime())
                    + EVERY + secondsToStringTime(task.getRepeatInterval
                    ()));
        } else if (task.isActive() && !task.isRepeated()) {
            result = result.concat(AT + DATE_F.get().format
                    (task.getStartTime()));
        }

        return result;
    }


}
