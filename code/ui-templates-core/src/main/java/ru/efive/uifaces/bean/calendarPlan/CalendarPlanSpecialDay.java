package ru.efive.uifaces.bean.calendarPlan;

import java.util.Date;

/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanSpecialDay extends CalendarPlanEvent
        implements Cloneable {

    public enum Category {

        dayOff, weekEnd, holyday, excluded
    }
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public CalendarPlanSpecialDay() {
    }

    public CalendarPlanSpecialDay(String title, Date start, Date stop, Category category) {
        super(title, start, stop);
        this.category = category;
    }

    public CalendarPlanSpecialDay slice(final Date start, final Date stop) {
        Date nstart = getStart(), nstop = getStop();
        if (start != null && (nstart == null || nstart.compareTo(start) <= 0)) {
            nstart = start;
        }
        if (stop != null && (nstop == null || nstop.compareTo(stop) > 0)) {
            nstop = stop;
        }
        if (nstart != null && nstop != null && nstart.compareTo(nstop) >= 0) {
            return null;
        }
        try {
            CalendarPlanSpecialDay result = (CalendarPlanSpecialDay) this.clone();
            result.setStart(nstart);
            result.setStop(nstop);
            return result;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
