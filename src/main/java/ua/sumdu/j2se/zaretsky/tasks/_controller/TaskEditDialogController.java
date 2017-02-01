package ua.sumdu.j2se.zaretsky.tasks._controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ua.sumdu.j2se.zaretsky.tasks._model.Task;
import ua.sumdu.j2se.zaretsky.tasks._util.DateUtil;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class _controller for task edit window.
 */
public class TaskEditDialogController {

    @FXML
    TextField textFieldTitle;
    @FXML
    DatePicker datePikedDateStart;
    @FXML
    ChoiceBox<String> choiseBoxHoursStart;
    @FXML
    ChoiceBox<String> choiseBoxMinutesStart;
    @FXML
    TextField repeatTime;
    @FXML
    DatePicker datePikedDateEnd;
    @FXML
    ChoiceBox<String> choiseBoxHoursEnd;
    @FXML
    ChoiceBox<String> choiseBoxMinutesEnd;
    @FXML
    CheckBox chkBoxActive;

    @FXML
    Button btnNewTaskOk;

    private Stage dialogStage;
    private Task task;
    private boolean okClicked = false;

    private static final List<String> HOURS = new ArrayList<>();
    private static final List<String> MINUTES = new ArrayList<>();

    public Task getTask() {
        return task;
    }

    /**
     * Static block use to fill
     */
    static {
        if (HOURS.isEmpty()) {
            for (int i = 0; i < 24; i++) {
                if (i < 10) {
                    HOURS.add("0" + i);
                } else
                    HOURS.add(String.valueOf(i));
            }
        }
        if (MINUTES.isEmpty()) {
            for (int j = 0; j < 60; j++) {

                if (j < 10) {
                    MINUTES.add("0" + j);
                } else
                    MINUTES.add(String.valueOf(j));
            }
        }
    }

    /**
     * Initialization _controller-class. This method call automatically after fxml file load.
     * Set values of choiseBox.
     */
    @FXML
    private void initialize() {
        Locale.setDefault(Locale.ENGLISH);

        choiseBoxHoursStart.setItems(FXCollections.observableArrayList(HOURS));
        choiseBoxMinutesStart.setItems(FXCollections.observableArrayList(MINUTES));

        choiseBoxHoursEnd.setItems(FXCollections.observableArrayList(HOURS));
        choiseBoxMinutesEnd.setItems(FXCollections.observableArrayList(MINUTES));
    }

    /**
     * Method set values of elements if it is edit task.
     *
     * @param task - task for edit
     */
    public void setTask(Task task) {
        this.task = task;

        textFieldTitle.setText(task.getTitle());
        chkBoxActive.setSelected(task.isActive());

        datePikedDateStart.setValue(task.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        choiseBoxHoursStart.setValue(DateUtil.choiceBoxTime(task.getStartTime().getHours()));
        choiseBoxMinutesStart.setValue(DateUtil.choiceBoxTime(task.getStartTime().getMinutes()));

        if (task.isRepeated()) {
            datePikedDateEnd.setValue(task.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            choiseBoxHoursEnd.setValue(DateUtil.choiceBoxTime(task.getEndTime().getHours()));
            choiseBoxMinutesEnd.setValue(DateUtil.choiceBoxTime(task.getEndTime().getMinutes()));

            repeatTime.setText(DateUtil.secondsToStringTime(task.getRepeatInterval()));
        } else {
            choiseBoxHoursEnd.setValue("00");
            choiseBoxMinutesEnd.setValue("00");
        }

    }

    /**
     * Method set values of elements if it is create new task.
     *
     * @param task - created task
     */
    public void setNewTask(Task task) {
        this.task = task;

        textFieldTitle.setText("");
        chkBoxActive.setSelected(false);

        datePikedDateStart.setValue(null);
        choiseBoxHoursStart.setValue("00");
        choiseBoxMinutesStart.setValue("00");

        datePikedDateEnd.setValue(null);
        choiseBoxHoursEnd.setValue("00");
        choiseBoxMinutesEnd.setValue("00");

        repeatTime.setText("");

    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Method gather all user input and apply it to task.
     * Called on clicked Ok button.
     */
    @FXML
    private void handleOk() throws ParseException {
        if (isInputValid()) {
            int repeatInterval;
            task.setTitle(textFieldTitle.getText());
            task.setActive(chkBoxActive.isSelected());

            //----------------------------------
            LocalDate localDateStart = datePikedDateStart.getValue();
            Instant instant1 = Instant.from(localDateStart.atStartOfDay(ZoneId
                    .systemDefault()));
            Long dateStartLong = Date.from(instant1).getTime();

            Integer hoursStart = Integer.parseInt(choiseBoxHoursStart.getValue()) *
                    60 * 60 * 1000;
            Integer minutesStart = Integer.parseInt(choiseBoxMinutesStart.getValue()) *
                    60 * 1000;

            Date dateStart = new Date(dateStartLong + hoursStart + minutesStart);//Get start time
            //--------------------------------


            repeatInterval = DateUtil.parseInterval(repeatTime.getText());
            if (repeatInterval > 0) {

                LocalDate localDateEnd = datePikedDateEnd.getValue();
                Instant instant2 = Instant.from(localDateEnd.atStartOfDay(ZoneId
                        .systemDefault()));
                Long dateEndLong = Date.from(instant2).getTime();

                Integer hoursEnd = Integer.parseInt(choiseBoxHoursEnd.getValue()) *
                        60 * 60 * 1000;
                Integer minutesEnd = Integer.parseInt(choiseBoxMinutesEnd.getValue()) *
                        60 * 1000;

                Date dateEnd = new Date(dateEndLong + hoursEnd + minutesEnd);//Get  end time
                //-------------------------------------

                task.setTime(dateStart, dateEnd, repeatInterval);
            } else {
                task.setTime(dateStart);
            }

            okClicked = true;
            dialogStage.close();
        }

    }

    /**
     * Method closes the window without saving changes.
     * Called on clicked Cancel button.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Method validates user input
     *
     * @return true, if user input is correct
     */
    private boolean isInputValid() {
        int rTime = 0;
        LocalDate start = null;

        if (titleNoValid()) {
            return showError("No valid title!");
        }
        if (startTimeNoValid()) {
            return showError("Incorrect start date! Maybe you  not choice date or date is " +
                    "less 1970.01.01");
        } else {
            start = datePikedDateStart.getValue();
        }
        try {
            rTime = DateUtil.parseInterval(repeatTime.getText());

        } catch (ParseException e) {
            return showError("Incorrect repeat time!");
        } catch (IllegalArgumentException a) {
            return showError(a.getMessage());
        }
        if (rTime > 0) {
            if (endTimeNoValid()) {
                return showError("Incorrect end date! Maybe you not choice date or date is less " +
                        "1970.01.01");
            }
            if (datePikedDateEnd.getValue().isBefore(start)) {
                return showError("Incorrect end date! End date is less then start date!");
            }
        }
        return true;
    }

    private boolean titleNoValid() {
        return textFieldTitle.getText() == null || textFieldTitle.getText().isEmpty() ||
                textFieldTitle.getText().matches("\\s+");
    }

    private boolean startTimeNoValid(){
        return datePikedDateStart.getValue() == null || datePikedDateStart
                .getValue().isBefore(DateUtil.dateToLaocalDate(Task.BEGIN));
    }

    private boolean endTimeNoValid(){
        return datePikedDateEnd.getValue() == null || datePikedDateEnd
                .getValue().isBefore(DateUtil.dateToLaocalDate(Task.BEGIN));
    }

    private boolean showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Invalid Fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(message);

        alert.showAndWait();
        return false;
    }

}
