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

    public static final String WORD_TO = " to ";
    public static final String WORD_FROM = " from ";
    public static final String WORD_AT = " at ";
    public static final String WORD_EVERY = " every ";

    public static final String TIME_PATTERN = "[yyyy-MM-dd HH:mm:ss.sss]";

    public static final ThreadLocal<DateFormat> DATE_F = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(TIME_PATTERN, Locale.ENGLISH);
        }
    };

    /**
     * Method write list with tasks in output stream of bytes.
     *
     * @param tasks - list with tasks
     * @param out   - output stream for writing bytes
     */
    public static void write(AbstractTaskList tasks, OutputStream out) throws IOException {
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

    /**
     * Method read stream of bytes in list with tasks.
     *
     * @param tasks - list with tasks
     * @param in    - input stream for reading bytes
     */
    public static void read(AbstractTaskList tasks, InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            int tasksCount = ois.readInt();
            for (int i = 0; i < tasksCount; i++) {
                int lengthTitle = ois.readInt();
                String title = (String) ois.readObject();
                if (lengthTitle != title.length()) {
                    throw new IOException("Incorrect title");
                }
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
    /**
     * Method write list with tasks in byte format to file.
     *
     * @param tasks - list with tasks
     * @param file   - file for writing bytes
     */
    public static void writeBinary(AbstractTaskList tasks, File file) throws
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
    /**
     * Method read tasks from binary file, and add to  AbstractTaskList.
     *
     * @param tasks - list with tasks
     * @param file   - file for reading bytes
     */
    public static void readBinary(AbstractTaskList tasks, File file) throws IOException,
            ClassNotFoundException {

        FileInputStream fis = new FileInputStream(file);
        read(tasks, fis);
        fis.close();


    }

    /**
     * Method write list with tasks in text format to character streams.
     *
     * @param tasks - list with tasks
     * @param out   - character stream
     */
    public static void write(AbstractTaskList tasks, Writer out) throws IOException {
        try {
            int numLine = 1;

            for (Task task : tasks) {
                out.write(taskToString(task));
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
    /**
     * Method read tasks from character stream and add it to AbstractTaskList.
     *
     * @param tasks - list with tasks
     * @param in   - character stream
     */
    public static void read(AbstractTaskList tasks, Reader in) throws IOException, ParseException {
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(in);
            String taskString;

            while ((taskString = bufferedReader.readLine()) != null) {
                Task task = TasksParser.parseTask(taskString);
                tasks.add(task);
            }
        } finally {
            assert bufferedReader != null;
            //noinspection ThrowFromFinallyBlock
            bufferedReader.close();
        }

    }

    /**
     * Method write list with tasks in text format to file.
     *
     * @param tasks - list with tasks
     * @param file   - file
     */
    public static void writeText(AbstractTaskList tasks, File file) throws
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
    /**
     * Method read tasks from text file and add it to AbstractTaskList.
     *
     * @param tasks - list with tasks
     * @param file   - file with tasks
     */
    public static void readText(AbstractTaskList tasks, File file) throws IOException, ParseException {
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

    /**
     * Method convert seconds in special text string
     *
     * @param timeSeconds - time in seconds
     * @return result - string, time in special text format
     */
    private static String secondsToStringTime(int timeSeconds) {
        String result;
        result = DateUtil.secondsToStringTime(timeSeconds);
        result = result.trim();
        return "[" + result + "]";
    }

    /**
     * Add double quotes if title have quotes
     *
     * @param title string without quotes
     * @return title with double quotes if necessary
     */
    private static String getTitleModification(String title) {
        return "\"" + title.replaceAll("\"", "\"\"") + "\"";
    }
    /**
     * This method convert task in special string format, for writing as text.
     *
     * @param task some task
     * @return result - text string
     */
    public static String taskToString(Task task) {
        String result = "";
        result = result.concat(getTitleModification(task.getTitle()));

        if (task.isActive()) {
            result = result.concat(task.isRepeated() ? WORD_FROM + DATE_F.get().format
                    (task.getStartTime()) + WORD_TO + DATE_F.get().format(task.getEndTime())
                    + WORD_EVERY + secondsToStringTime(task.getRepeatInterval()) : WORD_AT +
                    DATE_F.get().format(task.getStartTime()));
        } else {
            result = result.concat(task.isRepeated() ? WORD_FROM + DATE_F.get().format
                    (task.getStartTime()) + WORD_TO + DATE_F.get().format(task.getEndTime())
                    + WORD_EVERY + secondsToStringTime(task.getRepeatInterval()) + " is " +
                    "inactive" : WORD_AT + DATE_F.get().format(task.getStartTime()) + " is inactive");
        }
        return result;
    }


}
