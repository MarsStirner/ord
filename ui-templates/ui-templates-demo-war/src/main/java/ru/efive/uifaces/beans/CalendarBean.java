package ru.efive.uifaces.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanEvent;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanHolder;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanSpecialDay;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanSpecialDaysComposition;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "testCalendarBean")
@SessionScoped
public class CalendarBean {

    private CalendarPlanHolder holder;

    private static boolean isOccursInPeriod(int year, int month, int dayOfMonth, int hour, int minute,
            int durationUnit, int duration,
            Date start, Date stop) {
        if (start == null || stop == null) {
            return true;
        }
        Calendar c = new GregorianCalendar();
        c.setTime(start);
        if (year >= 0) {
            c.set(Calendar.YEAR, year);
        }
        if (month >= 0) {
            c.set(Calendar.MONTH, month);
        }
        if (dayOfMonth >= 0) {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        if (hour >= 0) {
            c.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0) {
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int y = c.get(Calendar.YEAR);
        if (start.compareTo(c.getTime()) > 0) {
            c.set(Calendar.YEAR, ++y);
        }
        if (stop.compareTo(c.getTime()) <= 0) {
            return false;
        }
        c.add(durationUnit, duration);
        if (stop.compareTo(c.getTime()) < 0) {
            return false;
        }
        return true;
    }

    private static boolean isOccursInDay(int month, int dayOfMonth, Date start, Date stop) {
        return isOccursInPeriod(-1, month, dayOfMonth, 0, 0, Calendar.DAY_OF_MONTH, 1, start, stop);
    }

    private static boolean isPeriodContains(int year, int month, int dayOfMonth, int hour, int minute,
            int durationUnit, int duration,
            Date start, Date stop) {
        if (start == null || stop == null) {
            return false;
        }
        Calendar c = new GregorianCalendar();
        c.setTime(start);
        if (year >= 0) {
            c.set(Calendar.YEAR, year);
        }
        if (month >= 0) {
            c.set(Calendar.MONTH, month);
        }
        if (dayOfMonth >= 0) {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        if (hour >= 0) {
            c.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0) {
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (start.compareTo(c.getTime()) < 0) {
            return false;
        }
        c.add(durationUnit, duration);
        if (stop.compareTo(c.getTime()) > 0) {
            return false;
        }
        return true;
    }

    private static boolean isDayContains(int month, int dayOfMonth, Date start, Date stop) {
        return isPeriodContains(-1, month, dayOfMonth, 0, 0, Calendar.DAY_OF_MONTH, 1, start, stop);
    }

    private static Date getFirstOccurenceInPeriod(int year, int month, int dayOfMonth, int hour, int minute,
            Date start, Date stop) {
        Calendar c = new GregorianCalendar();
        if (start != null) {
            c.setTime(start);
        }
        if (year >= 0) {
            c.set(Calendar.YEAR, year);
        }
        if (month >= 0) {
            c.set(Calendar.MONTH, month);
        }
        if (dayOfMonth >= 0) {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        if (hour >= 0) {
            c.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0) {
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int y = c.get(Calendar.YEAR);
        if (start != null && start.compareTo(c.getTime()) > 0) {
            c.set(Calendar.YEAR, ++y);
        }
        if (stop != null && stop.compareTo(c.getTime()) <= 0) {
            return null;
        }
        return c.getTime();
    }

    public CalendarBean() {
        CalendarPlanSpecialDay weekend = new CalendarPlanSpecialDay("weekend", null, null,
                CalendarPlanSpecialDay.Category.weekEnd) {

            @Override
            protected boolean testOccurs(Date start, Date stop) {
                if (start.after(stop)) {
                    return false;
                }
                Calendar cb = new GregorianCalendar(), ce = new GregorianCalendar();
                cb.setTime(start);
                ce.setTime(stop);
                int days = (int) ((stop.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));
                if (days < 7) {
                    do {
                        int cbd = cb.get(Calendar.DAY_OF_WEEK);
                        if (cbd == Calendar.SATURDAY || cbd == Calendar.SUNDAY) {
                            return true;
                        }
                        cb.add(Calendar.DAY_OF_MONTH, 1);
                    } while (cb.before(ce));
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            protected boolean testContains(Date start, Date stop) {
                if (start.after(stop)) {
                    return false;
                }
                Calendar cb = new GregorianCalendar();
                cb.setTime(start);
                int bDayOfWeek = cb.get(Calendar.DAY_OF_WEEK);
                if (bDayOfWeek == Calendar.SATURDAY || bDayOfWeek == Calendar.SUNDAY) {
                    do {
                        cb.add(Calendar.DAY_OF_MONTH, 1);
                    } while (cb.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY);
                    cb.set(Calendar.HOUR, 0);
                    cb.set(Calendar.MINUTE, 0);
                    cb.set(Calendar.SECOND, 0);
                    cb.set(Calendar.MILLISECOND, 0);
                    return cb.getTime().compareTo(stop) >= 0;
                }
                return false;
            }
        };

        Collection<CalendarPlanSpecialDay> days = new ArrayList<CalendarPlanSpecialDay>();
        Date bd, ed;
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MILLISECOND, 0);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, Calendar.MAY, 1, 0, 0, 0);
        bd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        ed = calendar.getTime();
        days.add(new CalendarPlanSpecialDay(null, bd, ed, CalendarPlanSpecialDay.Category.dayOff));

        days.add(new CalendarPlanSpecialDay(null, null, null, CalendarPlanSpecialDay.Category.holyday) {

            @Override
            public boolean isContains(Date start, Date stop) {
                return isDayContains(Calendar.JULY, 10, start, stop);
            }

            @Override
            public boolean isOccurs(Date start, Date stop) {
                return isOccursInDay(Calendar.JULY, 10, start, stop);
            }
        });

        days.add(new CalendarPlanSpecialDay(null, null, null, CalendarPlanSpecialDay.Category.holyday) {

            @Override
            public boolean isContains(Date start, Date stop) {
                return isDayContains(Calendar.NOVEMBER, 22, start, stop);
            }

            @Override
            public boolean isOccurs(Date start, Date stop) {
                return isOccursInDay(Calendar.NOVEMBER, 22, start, stop);
            }
        });

        final CalendarPlanSpecialDaysComposition specialDays =
                CalendarPlanSpecialDaysComposition.newDisplacement(weekend, days);
        final Collection<CalendarPlanEvent> events = new ArrayList<CalendarPlanEvent>();

        calendar.set(2012, Calendar.JANUARY, 26, 0, 0, 0);
        bd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        ed = calendar.getTime();
        events.add(new CalendarPlanEvent("developing calendar plan component", bd, ed) {

            @Override
            public Long getDuration() {
                return 3600000L * 4;
            }

            @Override
            public Date getFirstOccurence(Date start, Date stop) {
                return null;
            }
        });
        events.add(new CalendarPlanEvent("go to dinner :-)", null, null) {

            @Override
            public boolean isContains(Date start, Date stop) {
                return isPeriodContains(-1, -1, -1, 13, 0, Calendar.HOUR_OF_DAY, 1, start, stop);
            }

            @Override
            public boolean isOccurs(Date start, Date stop) {
                return isOccursInPeriod(-1, -1, -1, 13, 0, Calendar.HOUR_OF_DAY, 1, start, stop);
            }

            @Override
            public Date getFirstOccurence(Date start, Date stop) {
                return getFirstOccurenceInPeriod(-1, -1, -1, 13, 0, start, stop);
            }

            @Override
            public Long getDuration() {
                return 3600000L;
            }
        });

        holder = new CalendarPlanHolder() {

            @Override
            public Collection<CalendarPlanEvent> loadEvents(Date start, Date stop) {
                return events;
            }

            @Override
            public CalendarPlanSpecialDaysComposition loadSpecialDays(Date start, Date stop) {
                return specialDays.slice(start, stop);
            }
        };
    }

    public CalendarPlanHolder getHolder() {
        return holder;
    }
}
