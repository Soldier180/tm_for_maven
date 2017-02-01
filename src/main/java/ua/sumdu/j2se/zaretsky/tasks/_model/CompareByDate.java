package ua.sumdu.j2se.zaretsky.tasks._model;

import java.util.*;


/**
 * Created by Nikolion on 24.11.2016.
 */
public class CompareByDate implements Comparator<Date> {
    @Override
    public int compare(Date date1, Date date2) {
        if (date1.compareTo(date2) == 1) {
            return -1;
        }
        if (date1.compareTo(date2) == -1) {
            return 1;
        } else {
            return 0;
        }
//        return  date1.compareTo(date2);
    }
}
