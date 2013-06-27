package ru.efive.uifaces.bean.calendarPlan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanSpecialDaysComposition {

    protected Collection<CalendarPlanSpecialDay> days;

    public CalendarPlanSpecialDaysComposition() {
    }

    public CalendarPlanSpecialDaysComposition(Collection<CalendarPlanSpecialDay> days) {
        this.days = days;
    }

    public Collection<CalendarPlanSpecialDay> getDays() {
        return days;
    }

    public CalendarPlanSpecialDaysComposition slice(Date start, Date stop) {
        return newSlice(this, start, stop);
    }

    public static CalendarPlanSpecialDaysComposition newSlice(CalendarPlanSpecialDaysComposition days, Date start,
            Date stop) {
        final Collection<CalendarPlanSpecialDay> ndays = new ArrayList<CalendarPlanSpecialDay>();
        for (CalendarPlanSpecialDay d : days.days) {
            if (d.isOccurs(start, stop)) {
                ndays.add(d.slice(start, stop));
            }
        }
        return new CalendarPlanSpecialDaysComposition(ndays);
    }

    public static CalendarPlanSpecialDaysComposition newDisplacement(CalendarPlanSpecialDay day,
            CalendarPlanSpecialDay... days) {
        Collection<CalendarPlanSpecialDay> rdays = new ArrayList<CalendarPlanSpecialDay>();
        rdays.add(day);
        if (days != null && days.length != 0) {
            for (CalendarPlanSpecialDay d : days) {
                Collection<CalendarPlanSpecialDay> rrdays = new ArrayList<CalendarPlanSpecialDay>();
                for (CalendarPlanSpecialDay rday : rdays) {
                    CalendarPlanSpecialDay rrday;
                    rrday = rday.slice(null, d.getStart());
                    if (rrday != null) {
                        rrdays.add(rrday);
                    }
                    rrday = rday.slice(d.getStop(), null);
                    if (rrday != null) {
                        rrdays.add(rrday);
                    }
                }
                rdays = rrdays;
            }
            rdays.addAll(Arrays.asList(days));
        }
        return new CalendarPlanSpecialDaysComposition(rdays);
    }

    public static CalendarPlanSpecialDaysComposition newDisplacement(CalendarPlanSpecialDay day,
            Collection<CalendarPlanSpecialDay> days) {
        return newDisplacement(day, days.toArray(new CalendarPlanSpecialDay[0]));
    }

    public static CalendarPlanSpecialDaysComposition newUnion(final Collection<CalendarPlanSpecialDay>... days) {
        Collection<CalendarPlanSpecialDay> ndays = new ArrayList<CalendarPlanSpecialDay>();
        for (Collection<CalendarPlanSpecialDay> ds : days) {
            if (ds != null) {
                ndays.addAll(ds);
            }
        }
        return new CalendarPlanSpecialDaysComposition(ndays);
    }

    public static CalendarPlanSpecialDaysComposition newUnion(final CalendarPlanSpecialDaysComposition... days) {
        Collection<CalendarPlanSpecialDay> ndays = new ArrayList<CalendarPlanSpecialDay>();
        for (CalendarPlanSpecialDaysComposition ds : days) {
            if (ds != null) {
                ndays.addAll(ds.days);
            }
        }
        return new CalendarPlanSpecialDaysComposition(ndays);
    }
}
