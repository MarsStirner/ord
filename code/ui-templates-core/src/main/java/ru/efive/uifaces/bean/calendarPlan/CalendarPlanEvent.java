package ru.efive.uifaces.bean.calendarPlan;

import java.util.Date;

/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanEvent implements Comparable<CalendarPlanEvent> {

    private String title;
    private Date start;
    private Date stop;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    protected boolean testOccurs(Date start, Date stop) {
        return true;
    }

    /**
     * Tests if the given period satisfies the entity.
     * @param start The beginning of the period, inclusive. {@code null} means unbound.
     * @param stop The ending of the period, exclusive. {@code null} means unbound.
     * @return {@code true} if the entity occurs in the given period otherwise {@code false}
     */
    public boolean isOccurs(Date start, Date stop) {
        if (getStop() != null && start != null && getStop().compareTo(start) <= 0) {
            return false;
        }
        if (getStart() != null && stop != null && getStart().compareTo(stop) >= 0) {
            return false;
        }
        return testOccurs(start, stop);
    }

    public Date getFirstOccurence(Date start, Date stop) {
        if (isOccurs(start, stop)) {
            return getStart();
        } else {
            return null;
        }
    }

    protected boolean testContains(Date start, Date stop) {
        return true;
    }

    /**
     * Tests if the entity satisfying period contains the given period.
     * @param start The beginning of the period, inclusive. {@code null} means unbound.
     * @param stop The ending of the period, exclusive. {@code null} means unbound.
     * @return {@code true} if the entity satisfying period contains the given period otherwise {@code false}
     */
    public boolean isContains(Date start, Date stop) {
        if (getStart() != null && (start == null || getStart().compareTo(start) > 0)) {
            return false;
        }
        if (getStop() != null && (stop == null || getStop().compareTo(stop) < 0)) {
            return false;
        }
        return testContains(start, stop);
    }

    public Long getDuration() {
        return getStart() != null && getStop() != null ? getStop().getTime() - getStart().getTime() : null;
    }

    public CalendarPlanEvent() {
    }

    public CalendarPlanEvent(String title, Date start, Date stop) {
        this.title = title;
        this.start = start;
        this.stop = stop;
    }

    @Override
    public int compareTo(CalendarPlanEvent o) {
        Date fo = getFirstOccurence(start, stop), ofo = o.getFirstOccurence(o.start, o.stop);
        if (fo == null) {
            return ofo == null ? 0 : -1;
        }
        if (ofo == null) {
            return fo == null ? 0 : 1;
        }
        return fo.compareTo(ofo);
    }
}
