package ua.sumdu.j2se.zaretsky.tasks;/**
 * Created by Nikolion on 06.01.2017.
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import ua.sumdu.j2se.zaretsky.tasks.Controller.AllTasksInPeriodController;
import ua.sumdu.j2se.zaretsky.tasks.Controller.TaskEditDialogController;
import ua.sumdu.j2se.zaretsky.tasks.Controller.TasksOverviewController;
import ua.sumdu.j2se.zaretsky.tasks.Model.*;
import ua.sumdu.j2se.zaretsky.tasks.Model.Task;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

//import static ua.sumdu.j2se.zaretsky.tasks.Model.Detector.PAUSE;

public class MainApp extends Application {
    private final Logger log = LogManager.getLogger(MainApp.class.getSimpleName());
    private static TaskList tasks = new LinkedTaskList();
    public static final File FILE = new File(MainApp.class.getResource("/Data/tasks.bin")
            .getFile());
    // public static final File FILE = new File("src/main/resources/tasks");
    private Detector detector;
    private ObservableList<Task> tasksData = FXCollections
            .observableArrayList();


    public static TaskList getTasks() {
        return tasks;
    }

    public ObservableList<Task> getTasksData() {
        return tasksData;
    }


    private Stage primaryStage;
    private VBox rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image
                ("file:" + MainApp.class.getResource("/image/task_manager1.png").getFile()));
        this.primaryStage.setMinWidth(730);
        this.primaryStage.setMinHeight(500);
        this.primaryStage.setTitle("TASK MANAGER");

        log.info("Open program");

        initRootLayout();

        detector = new Detector(tasks, 600000, this);
        detector.start();
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                writeInFile();
                log.traceExit();
                LogManager.shutdown();

            }
        });
    }

    public MainApp() {
        try {
            TaskIO.readBinary(tasks, FILE);

            tasksData.clear();
            //add tasks in ObservableList
            for (Task t : tasks) {
                tasksData.add(t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.catching(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.catching(e);
        }


    }


    /**
     * Инициализирует корневой макет.
     */
    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/View/TasksOverview.fxml"));
            rootLayout = loader.load();


            TasksOverviewController controller = loader.getController();
            controller.setMainApp(this);

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.catching(e);
        }

    }


    /**
     * Открывает диалоговое окно для изменения деталей задачи.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте и возвращается значение true.
     *
     * @param task - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showTaskEditDialog(Task task, boolean newTask) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/View/TaskEditDialog.fxml"));
            GridPane page = (GridPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image
                    ("file:" + MainApp.class.getResource("/image/task_manager1.png").getFile()));
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
            e.printStackTrace();
            log.catching(e);
            return false;
        }
    }

    public void refreshTasks() {
        tasksData.clear();
        for (Task t : tasks) {
            tasksData.add(t);
        }
    }

    public void showAllTasksInPeriod(Date startPeriod, Date endPeriod) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/View/AllTasksInPeriod.fxml"));
            HBox page = (HBox) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image
                    ("file:" + MainApp.class.getResource("/image/task_manager1.png").getFile()));

            dialogStage.setTitle("All task in period");
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);


            AllTasksInPeriodController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDates(startPeriod, endPeriod);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * Возвращает главную сцену.
     *
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }


    private void writeInFile() {
        try {
            TaskIO.writeBinary(tasks, FILE);
        } catch (IOException e) {
            e.printStackTrace();
            log.catching(e);
        }
    }

    public static void main(String[] args) {

        launch(args);


    }


}

