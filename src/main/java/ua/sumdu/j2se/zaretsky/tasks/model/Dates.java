package ua.sumdu.j2se.zaretsky.tasks.model;

import java.util.Date;

/**
 * Class for working with date, but it unsupported method setTime
 * stream.
 */
public class Dates extends Date {

    public Dates(long time) {
        super(time);
    }
    public Dates() {
        super();
    }
    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException("Date should not be changed");
    }
    @Override
    public Date clone() {
        return new Date(getTime());
    }

}