package ua.sumdu.j2se.zaretsky.tasks.model;

import java.util.*;

import static ua.sumdu.j2se.zaretsky.tasks.model.Task.BEGIN;


public class Tasks {


    /**
     * Method to receive all the tasks what will be executed at least once in some period
     *
     * @param tasks     - list with tasks
     * @param startDate - start date for period
     * @param endDate   - end date for period
     * @return ArrayTaskList with tasks in some period
     */
    public static Iterable<Task> incoming(Iterable<Task> tasks, Date startDate,
                                          Date endDate) throws IllegalArgumentException {

        if (startDate.before(BEGIN) || endDate.before(BEGIN) || startDate.after(endDate)) {
            throw new IllegalArgumentException("Incorrect param from:" + startDate +
                    " or to:" + endDate);
        } else {
            AbstractTaskList list = new ArrayTaskList();
            for (Task task : tasks) {

                if (task.nextTimeAfter(startDate) != null && task.nextTimeAfter(startDate).compareTo(endDate) != 1) {
                    list.add(task);
                }
            }
            return list;
        }
    }

    /**
     * Method to find all task execution time for a predetermined period
     *
     * @param tasks - list with tasks
     * @param start - start date of period
     * @param end   - end date of period
     * @return SortedMap with key date and value set of tasks
     */
    public static SortedMap<Date, Set<Task>> calendar(Iterable<Task> tasks, Date
            start, Date end) {
        SortedMap<Date, Set<Task>> dateSetSortedMap = new TreeMap<>(new
                CompareByDate().reversed());
        Map<Date, Task> tempResult;
        for (Task task : tasks) {
            tempResult = allTasksInInterval(task, start, end);
            for (Map.Entry<Date, Task> taskIter : tempResult.entrySet()) {
                if (dateSetSortedMap.containsKey(taskIter.getKey())) {
                    dateSetSortedMap.get(taskIter.getKey()).add(taskIter.getValue());
                } else {
                    HashSet<Task> taskSet = new HashSet<Task>();
                    taskSet.add(taskIter.getValue());
                    dateSetSortedMap.put(taskIter.getKey(), taskSet);
                }
            }
        }
        return dateSetSortedMap;
    }
    /**
     * Method to find all task execution time for a predetermined period
     *
     * @param task - task
     * @param start - start date of period
     * @param end   - end date of period
     * @return Map with key date and value task
     */
    private static Map<Date, Task> allTasksInInterval(Task task, Date
            start, Date end) {
        TreeMap<Date, Task> result = new TreeMap<Date, Task>();
        Date nextStartTime = task.getStartTime();
        if (task.isRepeated()) {
            while (nextStartTime.compareTo(end) != 1 && nextStartTime
                    .compareTo(task.getEndTime()) != 1) {
                if (nextStartTime.compareTo(start) != -1) {
                    result.put(nextStartTime, task);
                }
                nextStartTime = new Date((nextStartTime.getTime() + (task
                        .getRepeatInterval() * 1000)));
            }
        } else if ((task.getStartTime().compareTo(start) != -1 && task.getStartTime().compareTo(end) != 1))
            result.put(nextStartTime, task);
        return result;
    }
}