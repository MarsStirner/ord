package ru.efive.uifaces.renderkit.html_basic;

import java.util.Calendar;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanPresentation;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanDayPresentation;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanHolder;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanMonthPresentation;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.CalendarPlan;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyle;
import static javax.faces.context.PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME;
import static javax.faces.context.PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS;

/**
/**
 * The renderer for {@link CalendarPlan}.
 *
 * @author Pavel Porubov
 */
@FacesRenderer(renderKitId = "HTML_BASIC",
    rendererType = CalendarPlanRenderer.RENDERER,
    componentFamily = ComponentFamily.CALENDAR_PLAN)
@ResourceDependencies({
    @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
    @ResourceDependency(name = "calendarPlan.js", target = "head", library = "e5ui/js"),
    @ResourceDependency(name = "calendarPlan.css", target = "head", library = "e5ui/css")})
public class CalendarPlanRenderer extends HtmlBasicRenderer {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER = "ru.efive.uifaces.CalendarPlan";

    /** The surrounding div's class. */
    public static final String CALENDAR_PLAN_CLASS = "e5uiCalendar";

    public static final String CONTAINER_CLASS = "container";
    public static final String CAPTION_CLASS = "caption";
    public static final String LAYOUT_CLASS = "layout";
    public static final String WIDGET_CLASS = "widget";
    public static final String SPACE_CLASS = "space";
    public static final String LINK_CLASS = "link";
    public static final String BUTTON_CLASS = "button";
    public static final String SELECT_CLASS = "select";

    public static final String YEAR_CLASS = "year";
    public static final String NEXT_YEAR_CLASS = "nextYear";
    public static final String PREV_YEAR_CLASS = "prevYear";

    public static final String MONTH_CLASS = "month";
    public static final String NEXT_MONTH_CLASS = "nextMonth";
    public static final String PREV_MONTH_CLASS = "prevMonth";

    public static final String WEEK_CLASS = "week";
    public static final String NEXT_WEEK_CLASS = "nextWeek";
    public static final String PREV_WEEK_CLASS = "prevWeek";

    public static final String DAY_CLASS = "day";
    public static final String NEXT_DAY_CLASS = "nextDay";
    public static final String PREV_DAY_CLASS = "prevDay";

    public static final String HEADER_CLASS = "header";
    public static final String CELL_CLASS = "cell";

    public static final String YEAR_NAME_CLASS = "yearName";
    public static final String MONTH_NAME_CLASS = "monthName";
    public static final String WEEK_NAME_CLASS = "weekName";
    public static final String WEEK_DATE_CLASS = "weekDate";
    public static final String DAY_NAME_CLASS = "dayName";
    public static final String DAY_DATE_CLASS = "dayDate";
    public static final String HOUR_DATE_CLASS = "hourDate";

    public static final String WEEKEND_CLASS = "weekend";
    public static final String HOLYDAY_CLASS = "holyday";
    public static final String DAYOFF_CLASS = "dayoff";
    public static final String EXCLUDED_DAY_CLASS = "excluded";
    public static final String NOW_DAY_CLASS = "nowDay";
    public static final String WITH_EVENTS_CLASS = "withEvents";
    public static final String EVENT_CLASS = "event";
    public static final String EVENT_NUMBER_CLASS_FMT = "en-%s-%s";
    public static final String HIGHLIGHT_CLASS = "highlight";
    public static final String HIGHLIGHT_EVENT_SCRIPT = "e5ui_calendarPlan.highlightEvent('%s', %s);";

    public static final String UPDATE_PRESENTATION_SCRIPT = "e5ui_calendarPlan.updatePresentation('%s', {%s: %s});";
    public static final String NEXT_YEAR_EVENT = "nextYear";
    public static final String PREV_YEAR_EVENT = "prevYear";
    public static final String SELECT_YEAR_EVENT = "selectYear";
    public static final String CHANGE_YEAR_LAYOUT_EVENT = "changeYearLayout";
    public static final String NEXT_MONTH_EVENT = "nextMonth";
    public static final String PREV_MONTH_EVENT = "prevMonth";
    public static final String SELECT_MONTH_EVENT = "selectMonth";
    public static final String CHANGE_MONTH_LAYOUT_EVENT = "changeMonthLayout";
    public static final String NEXT_WEEK_EVENT = "nextWeek";
    public static final String PREV_WEEK_EVENT = "prevWeek";
    public static final String SELECT_WEEK_EVENT = "selectWeek";
    public static final String CHANGE_WEEK_LAYOUT_EVENT = "changeWeekLayout";
    public static final String NEXT_DAY_EVENT = "nextDay";
    public static final String PREV_DAY_EVENT = "prevDay";
    public static final String SELECT_DAY_EVENT = "selectDay";
    public static final String CHANGE_DAY_LAYOUT_EVENT = "changeDayLayout";

    /** {@inheritDoc} */
    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        UIComponent uicomponent = writer.getComponent();
        if (!uicomponent.isRendered() || !(uicomponent instanceof CalendarPlan)) {
            return;
        }
        CalendarPlan component = (CalendarPlan) uicomponent;
        String id = component.getClientId();
        
        CalendarPlanHolder holder = component.getHolder();
        if (holder == null) {
            return;
        }
        CalendarPlanPresentation presentation = holder.getPresentation();

        //frame
        writer.startElement(HtmlElement.DIV);
        writer.writeAttribute(HtmlAttribute.ID, id, null);
        writeStyleClass(component, writer, CALENDAR_PLAN_CLASS, presentation.getName());
        writeStyle(component, writer);

        presentation.render(writer, holder);
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeChildren(AdvancedResponseWriter writer,
            List<UIComponent> children) throws IOException {
        return;
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        UIComponent component = writer.getComponent();
        if (!component.isRendered() || !(component instanceof CalendarPlan)) {
            return;
        }
        
        writer.endElement(HtmlElement.DIV); //frame
    }

    /** {@inheritDoc} */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        super.decode(context, component);
        if (component instanceof CalendarPlan) {
            CalendarPlan calendar = (CalendarPlan) component;
            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
            String execute = requestParameterMap.get(PARTIAL_EXECUTE_PARAM_NAME);
            if (execute != null && !execute.isEmpty()) {
                boolean event = execute.contains(ALL_PARTIAL_PHASE_CLIENT_IDS);
                if (!event) {
                    String id = calendar.getClientId();
                    for (String ex : execute.split("\\s+")) {
                        if (id.equals(ex)) {
                            event = true;
                            break;
                        }
                    }
                }
                if (event) {
                    CalendarPlanHolder holder = calendar.getHolder();
                    Calendar viewCalendar = holder.getViewCalendar();
                    //year
                    if (requestParameterMap.containsKey(NEXT_YEAR_EVENT)) {
                        viewCalendar.add(Calendar.YEAR, 1);
                    } else if (requestParameterMap.containsKey(PREV_YEAR_EVENT)) {
                        viewCalendar.add(Calendar.YEAR, -1);
                    } else if (requestParameterMap.containsKey(SELECT_YEAR_EVENT)) {
                        holder.setPresentation(holder.getYearPresentation());
                        viewCalendar.set(Calendar.YEAR, Integer.valueOf(requestParameterMap.get(SELECT_YEAR_EVENT)));
                    } else if (requestParameterMap.containsKey(CHANGE_YEAR_LAYOUT_EVENT)) {
                        ((CalendarPlanYearPresentation) holder.getYearPresentation()).setLayout(
                                CalendarPlanYearPresentation.Layout.valueOf(
                                requestParameterMap.get(CHANGE_YEAR_LAYOUT_EVENT)));
                    } else //month
                    if (requestParameterMap.containsKey(NEXT_MONTH_EVENT)) {
                        viewCalendar.add(Calendar.MONTH, 1);
                    } else if (requestParameterMap.containsKey(PREV_MONTH_EVENT)) {
                        viewCalendar.add(Calendar.MONTH, -1);
                    } else if (requestParameterMap.containsKey(SELECT_MONTH_EVENT)) {
                        holder.setPresentation(holder.getMonthPresentation());
                        String[] month = requestParameterMap.get(SELECT_MONTH_EVENT).split("-");
                        viewCalendar.set(Integer.valueOf(month[0]), Integer.valueOf(month[1]), 1, 0, 0, 0);
                        viewCalendar.set(Calendar.MILLISECOND, 0);
                    } else if (requestParameterMap.containsKey(CHANGE_MONTH_LAYOUT_EVENT)) {
                        ((CalendarPlanMonthPresentation) holder.getMonthPresentation()).setLayout(
                                CalendarPlanMonthPresentation.Layout.valueOf(
                                requestParameterMap.get(CHANGE_MONTH_LAYOUT_EVENT)));
                    } else //week
                    if (requestParameterMap.containsKey(NEXT_WEEK_EVENT)) {
                        viewCalendar.add(Calendar.DAY_OF_WEEK, 7);
                    } else if (requestParameterMap.containsKey(PREV_WEEK_EVENT)) {
                        viewCalendar.add(Calendar.DAY_OF_WEEK, -7);
                    } else if (requestParameterMap.containsKey(SELECT_WEEK_EVENT)) {
                        holder.setPresentation(holder.getWeekPresentation());
                        String[] week = requestParameterMap.get(SELECT_WEEK_EVENT).split("-");
                        viewCalendar.set(Integer.valueOf(week[0]), Integer.valueOf(week[1]), Integer.valueOf(week[2]),
                                0, 0, 0);
                        viewCalendar.set(Calendar.MILLISECOND, 0);
                    } else if (requestParameterMap.containsKey(CHANGE_WEEK_LAYOUT_EVENT)) {
                        ((CalendarPlanWeekPresentation) holder.getWeekPresentation()).setLayout(
                                CalendarPlanWeekPresentation.Layout.valueOf(
                                requestParameterMap.get(CHANGE_WEEK_LAYOUT_EVENT)));
                    } else //day
                    if (requestParameterMap.containsKey(NEXT_DAY_EVENT)) {
                        viewCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    } else if (requestParameterMap.containsKey(PREV_DAY_EVENT)) {
                        viewCalendar.add(Calendar.DAY_OF_YEAR, -1);
                    } else if (requestParameterMap.containsKey(SELECT_DAY_EVENT)) {
                        holder.setPresentation(holder.getDayPresentation());
                        String[] day = requestParameterMap.get(SELECT_DAY_EVENT).split("-");
                        viewCalendar.set(Integer.valueOf(day[0]), Integer.valueOf(day[1]), Integer.valueOf(day[2]),
                                0, 0, 0);
                        viewCalendar.set(Calendar.MILLISECOND, 0);
                    } else if (requestParameterMap.containsKey(CHANGE_DAY_LAYOUT_EVENT)) {
                        ((CalendarPlanDayPresentation) holder.getDayPresentation()).setLayout(
                                CalendarPlanWeekPresentation.Layout.valueOf(
                                requestParameterMap.get(CHANGE_DAY_LAYOUT_EVENT)));
                    }
                }
            }
        }
    }
}
