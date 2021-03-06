package ua.sumdu.j2se.zaretsky.tasks;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ua.sumdu.j2se.zaretsky.tasks.controller.AllTasksInPeriodController;
import ua.sumdu.j2se.zaretsky.tasks.controller.TaskEditDialogController;
import ua.sumdu.j2se.zaretsky.tasks.controller.TasksOverviewController;
import ua.sumdu.j2se.zaretsky.tasks.model.*;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application class.
 */

public class MainApp extends Application {
    private final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static AbstractTaskList tasks = new LinkedTaskList();
    private File fileWithTasks = null;
    private final ObservableList<Task> tasksData = FXCollections
            .observableArrayList();

    public static AbstractTaskList getTasks() {
        return tasks;
    }

    public ObservableList<Task> getTasksData() {
        return tasksData;
    }


    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/image/task_manager1.png")));
        this.primaryStage.setMinWidth(735);
        this.primaryStage.setMinHeight(500);
        this.primaryStage.setTitle("TASK MANAGER");

        initRootLayout();
        log.info("Open program");

        this.primaryStage.setOnCloseRequest(windowEvent -> {
            writeInFile();
            log.info("Exit");

        });
    }

    /**
     * Constructor for Main Application
     */
    public MainApp() {
        try {
            String path = getPathOfProgram();
            String fileSeparator = System.getProperty("file.separator");
            String file = path + fileSeparator + "data" + fileSeparator + "tasks.bin";

            fileWithTasks = new File(file);
            if (!fileWithTasks.exists()) {
                createDirWithData();
            }
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }


        try {
            TaskIO.readBinary(tasks, fileWithTasks);
            tasksData.clear();
            //add tasks in ObservableList
            for (Task task : tasks) {
                tasksData.add(task);
            }
        } catch (ClassNotFoundException | IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * Method create directory "data" with file "tasks.bin"
     */
    private void createDirWithData() {

        try {
            String path = getPathOfProgram();

            String fileSeparator = System.getProperty("file.separator");
            String newDir = path + fileSeparator + "data" + fileSeparator;

            File file = new File(newDir);
            file.mkdir();
            fileWithTasks = new File(newDir + fileSeparator + "tasks.bin");
            AbstractTaskList tempList = new LinkedTaskList();
            Task tempTask = new Task("First task", new Date(), new Date(new Date().getTime() +
                    1800000), 60);
            tempTask.setActive(true);
            tempList.add(tempTask);
            TaskIO.writeBinary(tempList, fileWithTasks);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Initialize root layout
     *
     * @return parent path directory with application
     */
    private static String getPathOfProgram() throws UnsupportedEncodingException {
        URL url = MainApp.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");

        return new File(jarPath).getParentFile().getPath();
    }

    /**
     * Initialize root layout
     */
    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/TasksOverview.fxml"));
            VBox rootLayout = loader.load();

            TasksOverviewController controller = loader.getController();
            controller.setParameters(this);

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }


    /**
     * It opens a dialog to change the task details. If the user clicks OK, the changes are
     * stored in the supplied object and returns true.
     *
     * @param task - task, which need to change
     * @return true, if push Ok button, else - false.
     */
    public boolean showTaskEditDialog(Task task, boolean newTask) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/TaskEditDialog.fxml"));
            GridPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/image/task_manager1.png")));
            if (newTask) {
                dialogStage.setTitle("Create Task");
            } else {
                dialogStage.setTitle("Edit Task");
            }

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);


            TaskEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            if (newTask) {
                controller.setNewTask(task);
            } else {
                controller.setTask(task);
            }

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Method reload tasks from  AbstractTaskList in ObservableList<Task> tasksData
     */
    public void refreshTasks() {
        tasksData.clear();
        for (Task task : tasks) {
            tasksData.add(task);
        }
    }

    /**
     * Method initialize window "All tasks in period"
     *
     * @param startPeriod start date for period
     * @param endPeriod   end date for period
     */
    public void showAllTasksInPeriod(Date startPeriod, Date endPeriod) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/AllTasksInPeriod.fxml"));
            HBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/image/task_manager1.png")));

            dialogStage.setTitle("All tasks in period");
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);


            AllTasksInPeriodController controller = loader.getController();
            controller.fillTableView(startPeriod, endPeriod);

            dialogStage.showAndWait();

        } catch (IOException e) {
            log.error(e.getMessage(), e);

        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Method write all tasks in binary format to file
     */
    private void writeInFile() {
        try {
            TaskIO.writeBinary(tasks, fileWithTasks);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {

        launch(args);

    }


}

