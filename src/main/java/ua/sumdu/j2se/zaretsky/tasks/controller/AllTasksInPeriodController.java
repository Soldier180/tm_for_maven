package ua.sumdu.j2se.zaretsky.tasks.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ua.sumdu.j2se.zaretsky.tasks.MainApp;
import ua.sumdu.j2se.zaretsky.tasks.model.Task;
import ua.sumdu.j2se.zaretsky.tasks.model.Tasks;

import java.util.*;

/**
 * Class controller for All task in period window.
 */
public class AllTasksInPeriodController {

    @FXML
    TableView<Map.Entry<Date, Set<Task>>> allTasksInPeriodTable;
    @FXML
    TableColumn<Map.Entry<Date, Set<Task>>, String> dateColumn;
    @FXML
    TableColumn<Map.Entry<Date, Set<Task>>, String> taskTitleColumn;


    /**
     * Method fill table with active task in some period.
     *
     * @param startPeriod - start time for period
     * @param endPeriod   - end time for period, is bigger than startPeriod
     */
    public void fillTableView(Date startPeriod, Date endPeriod) {
        SortedMap<Date, Set<Task>> tasks = Tasks.calendar(MainApp.getTasks(),
                startPeriod, endPeriod);

        dateColumn.setCellValueFactory(property -> {
            // this callback returns property for just one cell, you can't use a loop here
            // for first column we use key
            return new SimpleStringProperty(property.getValue().getKey().toString());
        });


        taskTitleColumn.setCellValueFactory(property -> {
            Iterator iterator = property.getValue().getValue().iterator();
            String result = "";
            while (iterator.hasNext()) {
                result += ((Task) iterator.next()).getTitle() + "\n";
            }
            // for second column we use value
            return new SimpleStringProperty(result);
        });


        ObservableList<Map.Entry<Date, Set<Task>>> items = FXCollections
                .observableArrayList(tasks.entrySet());
        allTasksInPeriodTable.setItems(items);

        //noinspection unchecked
        allTasksInPeriodTable.getColumns().setAll(dateColumn, taskTitleColumn);

    }

}

