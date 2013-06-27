package ru.efive.uifaces.bean.calendarPlan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Pavel Porubov
 */
public abstract class CalendarPlanHolder {

    private Calendar calendar = new GregorianCalendar();
    private Calendar viewCalendar = new GregorianCalendar();
    private CalendarPlanPresentation presentation;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private CalendarPlanPresentation yearPresentation;
    private CalendarPlanPresentation monthPresentation;
    private CalendarPlanPresentation weekPresentation;
    private CalendarPlanPresentation dayPresentation;

    {
        yearPresentation = new CalendarPlanYearPresentation();
        monthPresentation = new CalendarPlanMonthPresentation();
        weekPresentation = new CalendarPlanWeekPresentation();
        dayPresentation = new CalendarPlanDayPresentation();
        presentation = monthPresentation;
    }

    public CalendarPlanPresentation getDayPresentation() {
        return dayPresentation;
    }

    public void setDayPresentation(CalendarPlanPresentation dayPresentation) {
        this.dayPresentation = dayPresentation;
    }

    public CalendarPlanPresentation getMonthPresentation() {
        return monthPresentation;
    }

    public void setMonthPresentation(CalendarPlanPresentation monthPresentation) {
        this.monthPresentation = monthPresentation;
    }

    public CalendarPlanPresentation getWeekPresentation() {
        return weekPresentation;
    }

    public void setWeekPresentation(CalendarPlanPresentation weekPresentation) {
        this.weekPresentation = weekPresentation;
    }

    public CalendarPlanPresentation getYearPresentation() {
        return yearPresentation;
    }

    public void setYearPresentation(CalendarPlanPresentation yearPresentation) {
        this.yearPresentation = yearPresentation;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        pcs.firePropertyChange("calendar", this.calendar, calendar);
        this.calendar = calendar;
    }

    public Calendar getViewCalendar() {
        return viewCalendar;
    }

    public void setViewCalendar(Calendar viewCalendar) {
        pcs.firePropertyChange("viewCalendar", this.viewCalendar, viewCalendar);
        this.viewCalendar = viewCalendar;
    }

    public CalendarPlanPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(CalendarPlanPresentation presentation) {
        pcs.firePropertyChange("presentation", this.presentation, presentation);
        this.presentation = presentation;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public abstract Collection<CalendarPlanEvent> loadEvents(Date start, Date stop);

    public abstract CalendarPlanSpecialDaysComposition loadSpecialDays(Date start, Date stop);
}
