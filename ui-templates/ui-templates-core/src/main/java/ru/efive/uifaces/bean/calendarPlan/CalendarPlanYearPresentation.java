package ru.efive.uifaces.bean.calendarPlan;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttributeValue;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CAPTION_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WIDGET_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SPACE_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.LINK_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.MONTH_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.BUTTON_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.HEADER_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CELL_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAY_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WEEK_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.MONTH_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.YEAR_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAY_DATE_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WEEKEND_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.HOLYDAY_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.DAYOFF_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.EXCLUDED_DAY_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NOW_DAY_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.WITH_EVENTS_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.UPDATE_PRESENTATION_SCRIPT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_MONTH_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_WEEK_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CHANGE_YEAR_LAYOUT_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_DAY_EVENT;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation.getPeriodEvents;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation.getEventTitle;

import static java.lang.String.format;

/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanYearPresentation extends CalendarPlanPresentation {

    @Override
    public String getName() {
        return "year";
    }

    public static final int ID = 1;

    @Override
    public int getId() {
        return ID;
    }

    public static enum Layout {
        vertical3x4, vertical6x2, vertical2x6,
        horizontal3x4, horizontal6x2, horizontal2x6
    }
    private Layout layout = Layout.vertical3x4;

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public static Set<String> getSpecialDaysClassesForPeriod(CalendarPlanSpecialDaysComposition specialDays,
            Collection<CalendarPlanEvent> events, Date start, Date stop) {
        Set<String> res = new HashSet<String>();
        if (specialDays != null && specialDays.getDays() != null) {
            for (CalendarPlanSpecialDay d : specialDays.getDays()) {
                if (d.isContains(start, stop)) {
                    switch (d.getCategory()) {
                        case dayOff:
                            res.add(DAYOFF_CLASS);
                            break;
                        case excluded:
                            res.add(EXCLUDED_DAY_CLASS);
                            break;
                        case holyday:
                            res.add(HOLYDAY_CLASS);
                            break;
                        case weekEnd:
                            res.add(WEEKEND_CLASS);
                            break;
                    }
                }
            }
        }
        if (events != null) {
            for (CalendarPlanEvent e : events) {
                if (e.isOccurs(start, stop)) {
                    res.add(WITH_EVENTS_CLASS);
                }
            }
        }
        return res;
    }

    public static Set<String> getSpecialDaysClassesForDay(CalendarPlanSpecialDaysComposition specialDays,
            Collection<CalendarPlanEvent> events, Calendar calendar) {
        Date start = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date stop = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return getSpecialDaysClassesForPeriod(specialDays, events, start, stop);
    }

    private static void renderDay(AdvancedResponseWriter writer, int month, int nowDayOfYear, Calendar viewCalendar,
            CalendarPlanSpecialDaysComposition specialDays, Collection<CalendarPlanEvent> events) throws IOException {
        writer.startElement(HtmlElement.TD);
        Set<String> specialDaysClasses = getSpecialDaysClassesForDay(specialDays, events, viewCalendar);
        int currentMonth = viewCalendar.get(Calendar.MONTH);
        boolean excluded = (currentMonth == Calendar.JANUARY && month == Calendar.DECEMBER)
                || (currentMonth == Calendar.DECEMBER && month == Calendar.JANUARY);
        if (excluded) {
            specialDaysClasses.add(EXCLUDED_DAY_CLASS);
        }
        if (viewCalendar.get(Calendar.DAY_OF_YEAR) == nowDayOfYear) {
            specialDaysClasses.add(NOW_DAY_CLASS);
        }
        specialDaysClasses.add(CELL_CLASS);
        writeStyleClass(null, writer, specialDaysClasses.toArray(new String[0]));
        if (currentMonth == month || (currentMonth == Calendar.JANUARY && month == Calendar.DECEMBER)
                || (currentMonth == Calendar.DECEMBER && month == Calendar.JANUARY)) {
            String id = writer.getComponent().getClientId() + "-" + Long.toString(viewCalendar.getTimeInMillis());
            writer.startElement(HtmlElement.DIV);
            int day = viewCalendar.get(Calendar.DAY_OF_MONTH);
            if (!excluded) {
                writer.writeAttribute(HtmlAttribute.ID, id, null);
                writeStyleClass(null, writer, SELECT_CLASS, DAY_DATE_CLASS);
                writer.writeAttribute(HtmlAttribute.ONCLICK,
                        format(UPDATE_PRESENTATION_SCRIPT, writer.getComponent().getClientId(), SELECT_DAY_EVENT,
                        format(START_OF_WEEK_FMT, viewCalendar.get(Calendar.YEAR), month, day)), null);
            }
            writer.writeText(Integer.toString(day), null);
            writer.endElement(HtmlElement.DIV);

            if (!excluded) {
                writer.startElement(HtmlElement.SCRIPT);
                writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_TEXT_JAVASCRIPT, null);
                StringBuilder txt = new StringBuilder();
                txt.append("e5ui_calendarPlan.initDayTooltip(document.getElementById('").append(id).append("'), [");
                ArrayList<CalendarPlanEvent> dayEvents = new ArrayList<CalendarPlanEvent>();
                Date start = viewCalendar.getTime();
                viewCalendar.add(Calendar.DAY_OF_MONTH, 1);
                Date stop = viewCalendar.getTime();
                viewCalendar.add(Calendar.DAY_OF_MONTH, -1);
                getPeriodEvents(events, start, stop, dayEvents);
                for (CalendarPlanEvent e : dayEvents) {
                    txt.append("'").append(getEventTitle(e, start, stop, true)).append("',");
                }
                txt.setLength(txt.length() - 1);
                txt.append("]);");
                writer.writeText(txt.toString(), null);
                writer.endElement(HtmlElement.SCRIPT);

            }
        }
        writer.endElement(HtmlElement.TD);
    }

    public static final String START_OF_WEEK_FMT = "'%04d-%02d-%02d'";
    public static final String START_OF_MONTH_FMT = "'%04d-%02d'";

    private static void renderWeekLink(AdvancedResponseWriter writer, Calendar viewCalendar) throws IOException {
        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, CELL_CLASS, SPACE_CLASS);

        writer.startElement(HtmlElement.DIV);
        writeStyleClass(null, writer, SELECT_CLASS, WEEK_NAME_CLASS);
        writer.writeAttribute(HtmlAttribute.ONCLICK,
                format(UPDATE_PRESENTATION_SCRIPT, writer.getComponent().getClientId(), SELECT_WEEK_EVENT,
                format(START_OF_WEEK_FMT, viewCalendar.get(Calendar.YEAR), viewCalendar.get(Calendar.MONTH),
                viewCalendar.get(Calendar.DAY_OF_MONTH))), null);
        writer.writeText("+", null);
        writer.endElement(HtmlElement.DIV);

        writer.endElement(HtmlElement.TD);
    }

    public static void renderSpaceCell(AdvancedResponseWriter writer) throws IOException {
        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, SPACE_CLASS);
        writer.endElement(HtmlElement.TD);
    }

    public static ResourceBundle getStrs(Locale locale) {
        return ResourceBundle.getBundle(CalendarPlanYearPresentation.class.getPackage().getName()
                + ".calendarPlan", locale);
    }

    public static void renderLayoutLink(AdvancedResponseWriter writer, Enum layout,
            String cmd, String kind) throws IOException {
        String id = writer.getComponent().getClientId();

        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, LINK_CLASS);
        writer.startElement(HtmlElement.DIV);
        String yId = id + "-select" + kind +"Layout";
        writer.writeAttribute(HtmlAttribute.ID, yId, null);

        ResourceBundle strs = getStrs(writer.getContext().getViewRoot().getLocale());
        writer.writeText(strs.getString("layout.select"), null);

        writer.startElement(HtmlElement.SELECT);
        writer.writeAttribute(HtmlAttribute.ONCHANGE, format(UPDATE_PRESENTATION_SCRIPT, id, cmd,
                "this.value"), null);
        for (Enum l : layout.getClass().getEnumConstants()) {
            writer.startElement(HtmlElement.OPTION);
            writer.writeAttribute(HtmlAttribute.VALUE, l.name(), null);
            if (l == layout) {
                writer.writeAttribute(HtmlAttribute.SELECTED, true, null);
            }
            writer.writeText(strs.getString("layout." + l.name()), null);
            writer.endElement(HtmlElement.OPTION);
        }
        writer.endElement(HtmlElement.SELECT);

        writer.endElement(HtmlElement.DIV);
        writer.endElement(HtmlElement.TD);
    }

    public static void renderPrevNextLink(AdvancedResponseWriter writer, boolean next, String... cmds) throws IOException {
        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, LINK_CLASS);
        writer.startElement(HtmlElement.DIV);
        writeStyleClass(null, writer, BUTTON_CLASS, cmds[0]);
        writer.writeAttribute(HtmlAttribute.ONCLICK,
                format(UPDATE_PRESENTATION_SCRIPT, writer.getComponent().getClientId(), cmds[1], true), null);
        writer.writeText(next ? ">" : "<", null);
        writer.endElement(HtmlElement.DIV);
        writer.endElement(HtmlElement.TD);
    }

    public static void renderSelect(AdvancedResponseWriter writer, String... cmds) throws IOException {
        writer.startElement(HtmlElement.DIV);
        if (cmds[2] != null) {
            writeStyleClass(null, writer, SELECT_CLASS, cmds[1]);
            writer.writeAttribute(HtmlAttribute.ONCLICK, format(UPDATE_PRESENTATION_SCRIPT,
                    writer.getComponent().getClientId(), cmds[2], cmds[3]), null);
        } else {
            writeStyleClass(null, writer, cmds[1]);
        }
        writer.writeText(cmds[0], null);
        writer.endElement(HtmlElement.DIV);
    }

    public static void renderSelectLink(AdvancedResponseWriter writer, String... cmds) throws IOException {
        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, LINK_CLASS);
        renderSelect(writer, cmds);
        writer.endElement(HtmlElement.TD);
    }

    public static void renderMonthWidget(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            Calendar viewCalendar, int month, boolean vertical,
            Map<Integer, String> dayOfWeekNames) throws IOException {
        Date tm = viewCalendar.getTime();
        int year = viewCalendar.get(Calendar.YEAR);
        viewCalendar.set(year, month, 1, 0, 0, 0);
        viewCalendar.set(Calendar.MILLISECOND, 0);
        Date startOfMonthWidget = viewCalendar.getTime();
        
        Date start = viewCalendar.getTime();
        viewCalendar.add(Calendar.MONTH, 1);
        Date stop = viewCalendar.getTime();
        viewCalendar.add(Calendar.MONTH, -1);
        CalendarPlanSpecialDaysComposition specialDays = holder.loadSpecialDays(start, stop);
        Collection<CalendarPlanEvent> events = holder.loadEvents(start, stop);

        int nowDayOfYear, nowDayOfWeek;
        Calendar calendar = holder.getCalendar();
        if (viewCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == month) {
            nowDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        } else {
            nowDayOfYear = -1;
            nowDayOfWeek = -1;
        }

        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, WIDGET_CLASS, MONTH_CLASS);
        writer.startElement(HtmlElement.TBODY);

        int firstDayOfWeek = viewCalendar.getFirstDayOfWeek();
        while ((viewCalendar.get(Calendar.MONTH) == month && viewCalendar.get(Calendar.DAY_OF_MONTH) > 1)
                || viewCalendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            viewCalendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        writer.startElement(HtmlElement.TR);
        int dayOfWeek = firstDayOfWeek;
        do {
            int lastDay = (dayOfWeek == firstDayOfWeek ? 1
                    : dayOfWeek > firstDayOfWeek
                    ? dayOfWeek - firstDayOfWeek + 1 : dayOfWeek + 8 - firstDayOfWeek);

            writer.startElement(HtmlElement.TD);
            writeStyleClass(null, writer, HEADER_CLASS, dayOfWeek == nowDayOfWeek ? NOW_DAY_CLASS : null,
                    dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY ? WEEKEND_CLASS : null);
            writer.startElement(HtmlElement.DIV);
            writeStyleClass(null, writer, DAY_NAME_CLASS);
            writer.writeText(dayOfWeekNames.get(dayOfWeek), null);
            writer.endElement(HtmlElement.DIV);
            writer.endElement(HtmlElement.TD);

            if (vertical) {
                int dayOffset = 0;
                do {
                    renderDay(writer, month, nowDayOfYear, viewCalendar, specialDays, events);
                    viewCalendar.add(Calendar.DAY_OF_MONTH, 7);
                    dayOffset -= 7;
                } while (viewCalendar.get(Calendar.MONTH) == month
                        || viewCalendar.get(Calendar.DAY_OF_MONTH) < lastDay);

                if ((dayOfWeek < 7 ? dayOfWeek + 1 : 1) != firstDayOfWeek) {
                    viewCalendar.add(Calendar.DAY_OF_WEEK, dayOffset + 1);
                    writer.endElement(HtmlElement.TR);
                    writer.startElement(HtmlElement.TR);
                }
            }

            if (++dayOfWeek > 7) {
                dayOfWeek = 1;
            }
        } while (dayOfWeek != firstDayOfWeek);

        if (!vertical) {
            writer.startElement(HtmlElement.TD);
            writeStyleClass(null, writer, HEADER_CLASS, SPACE_CLASS);
            writer.endElement(HtmlElement.TD);
        }
        
        writer.endElement(HtmlElement.TR);

        if (!vertical) {
            do {
                writer.startElement(HtmlElement.TR);
                for (dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                    renderDay(writer, month, nowDayOfYear, viewCalendar, specialDays, events);
                    viewCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                viewCalendar.add(Calendar.DAY_OF_MONTH, -7);
                renderWeekLink(writer, viewCalendar);
                viewCalendar.add(Calendar.DAY_OF_MONTH, 7);
                writer.endElement(HtmlElement.TR);
            } while (viewCalendar.get(Calendar.MONTH) == month);
        } else {
            writer.startElement(HtmlElement.TR);

            writer.startElement(HtmlElement.TD);
            writeStyleClass(null, writer, HEADER_CLASS, SPACE_CLASS);
            writer.endElement(HtmlElement.TD);

            Date endOfMonthWidget = viewCalendar.getTime();
            viewCalendar.setTime(startOfMonthWidget);
            while (viewCalendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
                viewCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            do {
                renderWeekLink(writer, viewCalendar);
                viewCalendar.add(Calendar.DAY_OF_MONTH, 7);
            } while (viewCalendar.get(Calendar.MONTH) == month);
            viewCalendar.setTime(endOfMonthWidget);

            writer.endElement(HtmlElement.TR);
        }

        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);

        viewCalendar.setTime(tm);
    }

    private static void renderMonth(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            Calendar viewCalendar, int month, boolean vertical,
            Map<Integer, String>[] names) throws IOException {
        renderSelect(writer, names[MONTH_NAMES].get(month), MONTH_NAME_CLASS, SELECT_MONTH_EVENT,
                format(START_OF_MONTH_FMT, viewCalendar.get(Calendar.YEAR), month));
        renderMonthWidget(writer, holder, viewCalendar, month, vertical, names[DAY_OF_WEEK_NAMES]);
    }

    public static final int DAY_OF_WEEK_NAMES = 0;
    public static final int MONTH_NAMES = 1;

    public static Map<Integer, String>[] getDisplayNames(Calendar calendar, Locale locale) {
        Map<Integer, String> dayOfWeekNames = new HashMap<Integer, String>();
        for (Map.Entry<String, Integer> de :
                calendar.getDisplayNames(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale).entrySet()) {
            dayOfWeekNames.put(de.getValue(), de.getKey());
        }

        Map<Integer, String> monthNames = new HashMap<Integer, String>();
        for (Map.Entry<String, Integer> me :
                calendar.getDisplayNames(Calendar.MONTH, Calendar.LONG, locale).entrySet()) {
            monthNames.put(me.getValue(), me.getKey());
        }

        return new Map[]{dayOfWeekNames, monthNames};
    }

    private static void renderMonthsTable(AdvancedResponseWriter writer, CalendarPlanHolder holder,
            int rows, boolean vertical) throws IOException {
        Locale locale = writer.getContext().getViewRoot().getLocale();
        Calendar viewCalendar = (Calendar) holder.getViewCalendar().clone();

        int cols = 12 / rows;
        for (int r = 0; r < rows; r++) {
            writer.startElement(HtmlElement.TR);
            int month = vertical ? r + Calendar.JANUARY : r * cols + Calendar.JANUARY;
            for (int c = 0; c < cols; c++) {
                writer.startElement(HtmlElement.TD);
                renderMonth(writer, holder, viewCalendar, month, vertical, getDisplayNames(viewCalendar, locale));
                writer.endElement(HtmlElement.TD);
                if (vertical) {
                    month += rows;
                } else {
                    month++;
                }
            }
            writer.endElement(HtmlElement.TR);
        }
    }

    @Override
    public void render(AdvancedResponseWriter writer, CalendarPlanHolder holder) throws IOException {
        writer.startElement(HtmlElement.TABLE);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, CAPTION_CLASS, YEAR_CLASS);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);

        renderSpaceCell(writer);
        renderPrevNextLink(writer, false, PREV_YEAR_CLASS, PREV_YEAR_EVENT);
        renderSelectLink(writer, Integer.toString(holder.getViewCalendar().get(Calendar.YEAR)), YEAR_NAME_CLASS, null);
        renderPrevNextLink(writer, true, NEXT_YEAR_CLASS, NEXT_YEAR_EVENT);
        renderSpaceCell(writer);
        renderLayoutLink(writer, layout, CHANGE_YEAR_LAYOUT_EVENT, "Year");

        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, WIDGET_CLASS, YEAR_CLASS);
        writer.startElement(HtmlElement.TBODY);

        switch (layout) {
            case vertical2x6:
                renderMonthsTable(writer, holder, 2, true);
                break;
            case vertical3x4:
                renderMonthsTable(writer, holder, 3, true);
                break;
            case vertical6x2:
                renderMonthsTable(writer, holder, 6, true);
                break;
            case horizontal2x6:
                renderMonthsTable(writer, holder, 2, false);
                break;
            case horizontal3x4:
                renderMonthsTable(writer, holder, 4, false);
                break;
            case horizontal6x2:
                renderMonthsTable(writer, holder, 6, false);
                break;
        }

        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }
}
