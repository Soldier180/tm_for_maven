package ua.sumdu.j2se.zaretsky.tasks.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ua.sumdu.j2se.zaretsky.tasks.MainApp;
import ua.sumdu.j2se.zaretsky.tasks.model.*;
import ua.sumdu.j2se.zaretsky.tasks.util.DateUtil;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class controller for main window.
 */

public class TasksOverviewController {
    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, LocalDate> startTimeColumn;
    @FXML
    private TableColumn<Task, LocalDate> endTimeColumn;

    @FXML
    private Label titleLabel;
    @FXML
    private Label startTimeLabel;
    @FXML
    private Label endTimeLabel;
    @FXML
    private Label repeatLabel;
    @FXML
    private Label activityLabel;

    @FXML
    Button showBtn;
    @FXML
    DatePicker allTaskStartDatePiker;
    @FXML
    DatePicker allTaskEndDatePiker;

    private MainApp mainApp;
    private final Logger log = LoggerFactory.getLogger(TasksOverviewController.class.getSimpleName());
    public static final int PAUSE = 1000;//1 second
    public static final long NOTIFY_PERIOD = 1000;//1 second

    /**
     * Initialization controller-class. This method call automatically after fxml file load.
     * Set values of start and end period to view all tasks in it.
     */
    @FXML
    private void initialize() {
        Locale.setDefault(Locale.ENGLISH);
        allTaskStartDatePiker.setValue(LocalDate.now());
        allTaskEndDatePiker.setValue(allTaskStartDatePiker.getValue()
                .plusDays(7));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Task, String>
                ("title"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<Task, LocalDate>
                ("StartTime"));

        endTimeColumn.setCellValueFactory(new PropertyValueFactory<Task, LocalDate>
                ("EndTime"));
        showTaskDetails(null);

        tasksTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue));
    }

    /**
     * Set reference for main application, and TaskList with tasks.
     *
     * @param mainApp
     */
    public void setParameters(MainApp mainApp) {
        this.mainApp = mainApp;
        tasksTable.setItems(mainApp.getTasksData());
        runDetector();
    }

    private void runDetector() {
        javafx.concurrent.Task task = new javafx.concurrent.Task<Void>() {
            @Override
            public Void call() throws Exception {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            Date currentTime = new Date();
                            TaskList incomingTasks = (TaskList) Tasks.incoming(MainApp.getTasks(),
                                    currentTime, new
                                            Date(currentTime.getTime() + NOTIFY_PERIOD));
                            if (incomingTasks.count != 0) {

                                String messageText = "";
                                for (Task task : incomingTasks) {
                                    messageText += "Time: " + DateUtil.format(task.nextTimeAfter
                                            (currentTime));
                                    messageText += (" " + task.getTitle() + "\n");
                                }

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Attention");
                                alert.setHeaderText("Task time has come:");
                                alert.setContentText(messageText);
                                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                stage.getIcons().add(new Image(this.getClass().getResource("/image/task_manager1.png").toString()));

                                alert.showAndWait();
                            }
                        }
                    });
                    Thread.sleep(PAUSE);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Feel all text fields. If task = null - clear all fields.
     *
     * @param task — task type Task or null
     */
    private void showTaskDetails(Task task) {

        if (task == null) {// If task = null, remove all text.
            titleLabel.setText("");
            startTimeLabel.setText("");
            endTimeLabel.setText("");
            repeatLabel.setText("");
            activityLabel.setText("");
        } else {
            titleLabel.setText(task.getTitle());
            startTimeLabel.setText(DateUtil.format(task.getStartTime()));
            repeatLabel.setText(DateUtil.secondsToStringTime(task.getRepeatInterval()));

            if (task.getRepeatInterval() == 0) {
                endTimeLabel.setText("");
            } else {
                endTimeLabel.setText(DateUtil.format(task.getEndTime()));
            }

            activityLabel.setText(Boolean.toString(task.isActive()));
        }

    }

    /**
     * Method which delete selected task, with confirmation.
     * Called on clicked Delete button.
     */
    @FXML
    public void deleteTask() {
        int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Delete task");
            alert.setHeaderText("Are you sure you want delete the task?");
            alert.setContentText("Please confirm your choice");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Task tempTask = tasksTable.getItems().get(selectedIndex);
                String msg = TaskIO.taskToString(tempTask);
                MainApp.getTasks().remove(tempTask);
                tasksTable.getItems().remove(selectedIndex);
                log.info("DELETE task: " + msg);

            }

        } else {
            //No selected task
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No TASK Selected");
            alert.setContentText("Please select a TASK in the table.");

            alert.showAndWait();
        }
    }

    /**
     * Method which create new task and show task edit dialog
     * Called on clicked button New...
     */
    @FXML
    private void handleNewTask() {
        Task tempTask = new Task("default", new Date());
        boolean okClicked = mainApp.showTaskEditDialog(tempTask, true);
        if (okClicked) {
            mainApp.getTasksData().add(tempTask);
            MainApp.getTasks().add(tempTask);
            showTaskDetails(tempTask);
            log.info("CREATE task: " + TaskIO.taskToString(tempTask));
        }
    }

    /**
     * Method which edit current task and show task edit dialog
     * Called on clicked button Edit...
     */
    @FXML
    private void handleEditTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table.");

            alert.showAndWait();
        }

        String oldTask = TaskIO.taskToString(selectedTask);
        boolean okClicked = mainApp.showTaskEditDialog(selectedTask, false);
        if (okClicked) {
            tasksTable.getColumns().get(0).setVisible(false);
            tasksTable.getColumns().get(0).setVisible(true);
            showTaskDetails(selectedTask);
            String newTask = TaskIO.taskToString(selectedTask);
            if (!oldTask.equals(newTask)) {
                log.info("EDIT task: " + oldTask + " TO " + newTask);
            }
        }

    }

    /**
     * Method checks correctly dates and show all task in chosen period
     * Called on clicked button Show
     */
    @FXML
    private void handleShowAllTasksInPeriod() {

        Date startPeriod = DateUtil.localDateToDate(allTaskStartDatePiker
                .getValue().atTime(0, 0, 0));
        Date endPeriod = DateUtil.localDateToDate(allTaskEndDatePiker
                .getValue().atTime(23, 59, 59));
        if (startPeriod != null && endPeriod != null && endPeriod.compareTo
                (startPeriod) > 0) {


            mainApp.showAllTasksInPeriod(startPeriod, endPeriod);
        }

    }

    /**
     * Method which save current TaskList in text file.
     * Called on clicked menu Save as...
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();

        // Extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show saving file dialog
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }
            try {
                TaskIO.writeText(MainApp.getTasks(), file);
            } catch (IOException e) {
                log.error("Error:",e);
                showError();
            }
        }
    }

    /**
     * Method which add tasks from text file to current TaskList.
     * Called on clicked menu Load from text...
     */
    @FXML
    private void loadTasksFromTxtFile() {
        FileChooser fileChooser = new FileChooser();

        // Extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            try {
                TaskIO.readText(MainApp.getTasks(), file);
                mainApp.refreshTasks();
            } catch (IOException e) {
                log.error("Error:",e);
                showError();
            } catch (ParseException e) {
                log.error("Error:",e);
                showError();
            }

        }

    }

    /**
     * Method show error if load task from file not successful.
     */
    private void showError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle("Error");
        alert.setHeaderText("Attention - some error with load from file");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/image/task_manager1.png").toString()));
        alert.showAndWait();
    }

    /**
     * Method show information about application.
     * Called on clicked menu About.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TASK MANAGER");

        alert.setHeaderText("About: simple task manager");
        alert.setContentText("\tAuthor: Nikolay Zaretsky");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/image/task_manager1.png").toString()));

        alert.showAndWait();
    }
}


 /*   Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <1 ; i++) {

                }{
                    long currentTime = new Date().getTime();
                    TaskList schedule = (ArrayTaskList) Tasks
                            .incoming(mainApp.tasks, new Date
                                    (currentTime), new Date
                                    (currentTime + 300000));

                    for (Task j:schedule) {
                        titleTask.setText(j.getTitle());
                        Alert t = new Alert(Alert.AlertType.INFORMATION);
                        t.initOwner(mainApp.getPrimaryStage());
                        t.setTitle(j.getTitle());
                        t.show();
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });*/