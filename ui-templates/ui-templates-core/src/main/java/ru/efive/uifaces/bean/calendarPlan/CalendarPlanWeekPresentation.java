package ru.efive.uifaces.bean.calendarPlan;

import java.util.ResourceBundle;
import java.util.List;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;

import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CAPTION_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.LINK_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WIDGET_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.HEADER_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CELL_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.HOUR_DATE_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAY_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAY_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAY_DATE_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NOW_DAY_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.EVENT_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.EVENT_NUMBER_CLASS_FMT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.HIGHLIGHT_EVENT_SCRIPT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WEEK_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WEEK_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.MONTH_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.YEAR_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_WEEK_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_WEEK_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.UPDATE_PRESENTATION_SCRIPT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_DAY_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_WEEK_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_WEEK_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_MONTH_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CHANGE_WEEK_LAYOUT_EVENT;

import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.getDisplayNames;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.getSpecialDaysClassesForDay;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.getSpecialDaysClassesForPeriod;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.DAY_OF_WEEK_NAMES;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.MONTH_NAMES;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.START_OF_MONTH_FMT;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.START_OF_WEEK_FMT;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderSpaceCell;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderSelectLink;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderPrevNextLink;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderLayoutLink;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.getStrs;

import static java.lang.String.format;

/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanWeekPresentation extends CalendarPlanPresentation {

    @Override
    public String getName() {
        return "week";
    }

    public static final int ID = 3;

    @Override
    public int getId() {
        return ID;
    }

    public static enum Layout {
        byHours, byEvents
    }
    private Layout layout = Layout.byEvents;

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    private static final String DAY_DATE_OF_WEEK_FMT = "%02d.%02d";
    private static final String HOUR_DATE_OF_DAY_FMT = "%02d";
    private static final ThreadLocal<DateFormat> EVENT_OF_DAY_START_FMT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    private static final String DURATION_TIME_UNIT_FMT = "%02d";

    public static void renderYearLinkAndLayout(AdvancedResponseWriter writer, int year,
            Enum layout, String cmd, String kind) throws
            IOException {
        renderSpaceCell(writer);
        renderPrevNextLink(writer, false, PREV_YEAR_CLASS, PREV_YEAR_EVENT);
        String y = Integer.toString(year);
        renderSelectLink(writer, y, YEAR_NAME_CLASS, SELECT_YEAR_EVENT, y);
        renderPrevNextLink(writer, true, NEXT_YEAR_CLASS, NEXT_YEAR_EVENT);
        renderSpaceCell(writer);
        renderLayoutLink(writer, layout, cmd, kind);
    }

    public static void renderHourOfDays(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            Calendar viewCalendar, boolean allDay,
            CalendarPlanSpecialDaysComposition specialDays,
            Collection<CalendarPlanEvent> events,
            int days) throws IOException {

        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, HEADER_CLASS, HOUR_DATE_CLASS);
        
        ResourceBundle strs = getStrs(writer.getContext().getViewRoot().getLocale());
        if (allDay) {
            writer.writeText(strs.getString("table.allDay"), null);
        } else {
            writer.writeText(format(HOUR_DATE_OF_DAY_FMT, viewCalendar.get(Calendar.HOUR_OF_DAY)), null);
        }
        writer.endElement(HtmlElement.TD);

        ArrayList<CalendarPlanEvent> hourEvents = new ArrayList<CalendarPlanEvent>();
        for (int day = 0; day < days; day++) {
            writer.startElement(HtmlElement.TD);

            Date start = viewCalendar.getTime();
            viewCalendar.add(Calendar.HOUR_OF_DAY, 1);
            Date stop = viewCalendar.getTime();
            viewCalendar.add(Calendar.HOUR_OF_DAY, -1);

            Set<String> specialDaysClasses = getSpecialDaysClassesForPeriod(specialDays, null, start, stop);
            specialDaysClasses.add(CELL_CLASS);
            writeStyleClass(null, writer, specialDaysClasses.toArray(new String[0]));

            getPeriodEvents(events, start, stop, hourEvents);
            for (CalendarPlanEvent e : hourEvents) {
                if (allDay || e.getFirstOccurence(start, stop) != null) {
                    writer.startElement(HtmlElement.DIV);
                    String ec = format(EVENT_NUMBER_CLASS_FMT, writer.getComponent().getClientId(), e.hashCode());
                    writeStyleClass(null, writer, EVENT_CLASS, ec);
                    writer.writeAttribute(HtmlAttribute.ONMOUSEOVER,
                            format(HIGHLIGHT_EVENT_SCRIPT, ec, Boolean.TRUE.toString()), null);
                    writer.writeAttribute(HtmlAttribute.ONMOUSEOUT,
                            format(HIGHLIGHT_EVENT_SCRIPT, ec, Boolean.FALSE.toString()), null);
                    writer.writeText(getEventTitle(e, start, stop, false), null);
                    writer.endElement(HtmlElement.DIV);
                }
            }

            writer.endElement(HtmlElement.TD);
            viewCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public static void getPeriodEvents(Collection<CalendarPlanEvent> events,
            Date start, Date stop,
            List<CalendarPlanEvent> periodEvents) {
        periodEvents.clear();
        for (CalendarPlanEvent e : events) {
            if (e.isOccurs(start, stop)) {
                periodEvents.add(e);
            }
        }
        Collections.sort(periodEvents);
    }

    public static String getEventTitle(CalendarPlanEvent event, Date start, Date stop, boolean printStart) {
        StringBuilder txt = new StringBuilder();
        Date firstOccurence = printStart ? event.getFirstOccurence(start, stop) : null;
        if (firstOccurence != null) {
            txt.append(EVENT_OF_DAY_START_FMT.get().format(firstOccurence));
        }
        Long duration = event.getDuration();
        if (duration != null) {
            if (firstOccurence != null) {
                txt.append(", ");
            }
            txt.append("(");
            long h = duration / 3600000;
            if (h != 0) {
                txt.append(format(DURATION_TIME_UNIT_FMT, h)).append(":");
            }
            long m = (duration % 3600000) / 60000;
            if (m != 0 || h != 0) {
                txt.append(format(DURATION_TIME_UNIT_FMT, m));
            }
            long s = (duration % 6000) / 1000;
            if (s != 0 || h == 0 && m == 0) {
                if (m != 0 || h != 0) {
                    txt.append(":");
                }
                txt.append(format(DURATION_TIME_UNIT_FMT, s));
            }
            long ms = duration % 1000;
            if (ms != 0) {
                txt.append(".").append(format("%03d", ms));
            }
            txt.append(")");
        }
        if (firstOccurence != null || duration != null) {
            txt.append(": ");
        }
        txt.append(event.getTitle());
        return txt.toString();
    }

    public static void renderEventsOfDays(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            Calendar viewCalendar,
            CalendarPlanSpecialDaysComposition specialDays,
            Collection<CalendarPlanEvent> events,
            int days) throws IOException {
        ArrayList<CalendarPlanEvent> dayEvents = new ArrayList<CalendarPlanEvent>();
        Map<Integer, Integer> eventNumbers = new HashMap<Integer, Integer>();
        int eventNumberSequence = 0;
        Date start = null, stop = null;
        String id = writer.getComponent().getClientId();
        ResourceBundle strs = getStrs(writer.getContext().getViewRoot().getLocale());
        for (int day = 0; day < days; day++) {
            writer.startElement(HtmlElement.TD);
            start = stop != null ? stop : viewCalendar.getTime();
            viewCalendar.add(Calendar.DAY_OF_MONTH, 1);
            stop = viewCalendar.getTime();
            getPeriodEvents(events, start, stop, dayEvents);
            for (CalendarPlanEvent e : dayEvents) {
                int en;
                if (!eventNumbers.containsKey(e.hashCode())) {
                    eventNumbers.put(e.hashCode(), ++eventNumberSequence);
                    en = eventNumberSequence;
                } else {
                    en = eventNumbers.get(e.hashCode());
                }
                writer.startElement(HtmlElement.DIV);
                String ec = format(EVENT_NUMBER_CLASS_FMT, id, en);
                writeStyleClass(null, writer, EVENT_CLASS, ec);
                writer.writeAttribute(HtmlAttribute.ONMOUSEOVER,
                        format(HIGHLIGHT_EVENT_SCRIPT, ec, Boolean.TRUE.toString()), null);
                writer.writeAttribute(HtmlAttribute.ONMOUSEOUT,
                        format(HIGHLIGHT_EVENT_SCRIPT, ec, Boolean.FALSE.toString()), null);
                writer.writeText((e.getFirstOccurence(start, stop) == null ? strs.getString("table.allDay") + ", " : "")
                        + getEventTitle(e, start, stop, true), null);
                writer.endElement(HtmlElement.DIV);
            }
            writer.endElement(HtmlElement.TD);
        }
    }

    public static void renderDaysInWeek(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            Calendar viewCalendar, Date start, Date stop,
            Map<Integer, String> dayOfWeekNames,
            int firstDayOfWeek, Layout layout) throws IOException {
        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, WIDGET_CLASS, firstDayOfWeek >= 0 ? WEEK_CLASS : DAY_CLASS);
        writer.startElement(HtmlElement.TBODY);

        writer.startElement(HtmlElement.TR);

        if (layout == Layout.byHours) {
            writer.startElement(HtmlElement.TD);
            writeStyleClass(null, writer, HEADER_CLASS);
            writer.endElement(HtmlElement.TD);
        }

        CalendarPlanSpecialDaysComposition specialDays = holder.loadSpecialDays(start, stop);
        int currentYear = holder.getCalendar().get(Calendar.YEAR);
        int currentMonth = holder.getCalendar().get(Calendar.MONTH);
        int curentDay = holder.getCalendar().get(Calendar.DAY_OF_MONTH);
        String id = writer.getComponent().getClientId();
        viewCalendar.setTime(start);
        do {
            writer.startElement(HtmlElement.TD);

            Set<String> specialDaysClasses = getSpecialDaysClassesForDay(specialDays, null, viewCalendar);
            specialDaysClasses.add(HEADER_CLASS);
            int year = viewCalendar.get(Calendar.YEAR);
            int month = viewCalendar.get(Calendar.MONTH);
            int day = viewCalendar.get(Calendar.DAY_OF_MONTH);
            if (currentYear == year && currentMonth == month && curentDay == day) {
                specialDaysClasses.add(NOW_DAY_CLASS);
            }
            writeStyleClass(null, writer, specialDaysClasses.toArray(new String[0]));

            writer.startElement(HtmlElement.SPAN);
            writeStyleClass(null, writer, DAY_DATE_CLASS);
            writer.writeText(dayOfWeekNames.get(viewCalendar.get(Calendar.DAY_OF_WEEK)), null);
            writer.endElement(HtmlElement.SPAN);

            if (firstDayOfWeek >= 0) {
                writer.startElement(HtmlElement.SPAN);
                writeStyleClass(null, writer, SELECT_CLASS, DAY_NAME_CLASS);
                writer.writeAttribute(HtmlAttribute.ONCLICK,
                        format(UPDATE_PRESENTATION_SCRIPT, id, SELECT_DAY_EVENT,
                        format(START_OF_WEEK_FMT, year, month, day)), null);
                writer.writeText(format(DAY_DATE_OF_WEEK_FMT, day, month + 1), null);
                writer.endElement(HtmlElement.SPAN);
            }
            writer.endElement(HtmlElement.TD);

            if (firstDayOfWeek >= 0) {
                viewCalendar.add(Calendar.DAY_OF_WEEK, 1);
            }
        } while (firstDayOfWeek >= 0 && viewCalendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

        writer.endElement(HtmlElement.TR);

        Collection<CalendarPlanEvent> events = holder.loadEvents(start, stop);
        viewCalendar.setTime(start);
        if (layout == Layout.byHours) {
            writer.startElement(HtmlElement.TR);
            viewCalendar.set(Calendar.HOUR_OF_DAY, 0);
            renderHourOfDays(writer, holder, viewCalendar, true, specialDays, events, firstDayOfWeek >= 0 ? 7 : 1);
            writer.endElement(HtmlElement.TR);
            for (int hour = 0; hour < 24; hour++) {
                writer.startElement(HtmlElement.TR);
                viewCalendar.set(Calendar.HOUR_OF_DAY, hour);
                renderHourOfDays(writer, holder, viewCalendar, false, specialDays, events, firstDayOfWeek >= 0 ? 7 : 1);
                writer.endElement(HtmlElement.TR);
            }
        } else {
            writer.startElement(HtmlElement.TR);
            renderEventsOfDays(writer, holder, viewCalendar, specialDays, events, firstDayOfWeek >= 0 ? 7 : 1);
            writer.endElement(HtmlElement.TR);
        }

        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }

    @Override
    public void render(AdvancedResponseWriter writer, CalendarPlanHolder holder) throws IOException {
        String id = writer.getComponent().getClientId();

        writer.startElement(HtmlElement.TABLE);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        Locale locale = writer.getContext().getViewRoot().getLocale();
        Calendar viewCalendar = (Calendar) holder.getViewCalendar().clone();
        Map<Integer, String>[] names = getDisplayNames(viewCalendar, locale);

        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, CAPTION_CLASS, WEEK_CLASS);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);

        renderSpaceCell(writer);
        renderPrevNextLink(writer, false, PREV_WEEK_CLASS, PREV_WEEK_EVENT);

        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, LINK_CLASS);
        writer.startElement(HtmlElement.DIV);
        writeStyleClass(null, writer, WEEK_NAME_CLASS);

        int firstDayOfWeek = viewCalendar.getFirstDayOfWeek(),
                lastDayOfWeek = firstDayOfWeek > 1 ? firstDayOfWeek - 1 : 7;
        viewCalendar.set(Calendar.HOUR_OF_DAY, 0);
        viewCalendar.set(Calendar.MINUTE, 0);
        viewCalendar.set(Calendar.SECOND, 0);
        viewCalendar.set(Calendar.MILLISECOND, 0);
        while (viewCalendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            viewCalendar.add(Calendar.DAY_OF_WEEK, -1);
        }
        Date startTime = viewCalendar.getTime();
        int startMonth = viewCalendar.get(Calendar.MONTH);
        writer.startElement(HtmlElement.SPAN);
        writeStyleClass(null, writer, SELECT_CLASS, MONTH_NAME_CLASS);
        writer.writeAttribute(HtmlAttribute.ONCLICK,
                format(UPDATE_PRESENTATION_SCRIPT, id, SELECT_MONTH_EVENT,
                format(START_OF_MONTH_FMT, viewCalendar.get(Calendar.YEAR), startMonth)), null);
        writer.writeText(names[MONTH_NAMES].get(startMonth), null);
        writer.endElement(HtmlElement.SPAN);
        writer.writeText(format(" %d - ", viewCalendar.get(Calendar.DAY_OF_MONTH)), null);

        while (viewCalendar.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek) {
            viewCalendar.add(Calendar.DAY_OF_WEEK, 1);
        }
        int endMonth = viewCalendar.get(Calendar.MONTH);
        if (startMonth != endMonth) {
            writer.startElement(HtmlElement.SPAN);
            writeStyleClass(null, writer, SELECT_CLASS, MONTH_NAME_CLASS);
            writer.writeAttribute(HtmlAttribute.ONCLICK,
                    format(UPDATE_PRESENTATION_SCRIPT, id, SELECT_MONTH_EVENT,
                    format(START_OF_MONTH_FMT, viewCalendar.get(Calendar.YEAR), endMonth)), null);
            writer.writeText(names[MONTH_NAMES].get(endMonth), null);
            writer.endElement(HtmlElement.SPAN);
        } else {
            writer.writeText(names[MONTH_NAMES].get(endMonth), null);
        }
        writer.writeText(format(" %d", viewCalendar.get(Calendar.DAY_OF_MONTH)), null);

        writer.endElement(HtmlElement.DIV);
        writer.endElement(HtmlElement.TD);

        renderPrevNextLink(writer, true, NEXT_WEEK_CLASS, NEXT_WEEK_EVENT);
        renderYearLinkAndLayout(writer, viewCalendar.get(Calendar.YEAR), layout, CHANGE_WEEK_LAYOUT_EVENT, "Week");

        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        viewCalendar.add(Calendar.DAY_OF_WEEK, 1);
        Date endTime = viewCalendar.getTime();
        renderDaysInWeek(writer, holder, viewCalendar, startTime, endTime, names[DAY_OF_WEEK_NAMES],
                firstDayOfWeek, layout);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }
}
