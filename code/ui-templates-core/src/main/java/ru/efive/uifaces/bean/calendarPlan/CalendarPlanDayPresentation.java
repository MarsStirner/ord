package ru.efive.uifaces.bean.calendarPlan;

import ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation.Layout;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation.renderDaysInWeek;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanWeekPresentation.renderYearLinkAndLayout;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.*;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.*;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;

/**
 * @author Pavel Porubov
 */
public class CalendarPlanDayPresentation extends CalendarPlanPresentation {

    public static final int ID = 4;
    private Layout layout = Layout.byEvents;

    @Override
    public String getName() {
        return "day";
    }

    @Override
    public int getId() {
        return ID;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
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
        writeStyleClass(null, writer, CAPTION_CLASS, DAY_CLASS);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);

        renderSpaceCell(writer);
        renderPrevNextLink(writer, false, PREV_DAY_CLASS, PREV_DAY_EVENT);

        writer.startElement(HtmlElement.TD);
        writeStyleClass(null, writer, LINK_CLASS);
        writer.startElement(HtmlElement.DIV);
        writeStyleClass(null, writer, DAY_NAME_CLASS);

        viewCalendar.set(Calendar.HOUR_OF_DAY, 0);
        viewCalendar.set(Calendar.MINUTE, 0);
        viewCalendar.set(Calendar.SECOND, 0);
        viewCalendar.set(Calendar.MILLISECOND, 0);
        Date startTime = viewCalendar.getTime();
        int month = viewCalendar.get(Calendar.MONTH);
        writer.startElement(HtmlElement.SPAN);
        writeStyleClass(null, writer, SELECT_CLASS, MONTH_NAME_CLASS);
        writer.writeAttribute(HtmlAttribute.ONCLICK,
                format(UPDATE_PRESENTATION_SCRIPT, id, SELECT_MONTH_EVENT,
                        format(START_OF_MONTH_FMT, viewCalendar.get(Calendar.YEAR), month)), null);
        writer.writeText(names[MONTH_NAMES].get(month), null);
        writer.endElement(HtmlElement.SPAN);
        writer.writeText(format(" %d", viewCalendar.get(Calendar.DAY_OF_MONTH)), null);

        writer.endElement(HtmlElement.DIV);
        writer.endElement(HtmlElement.TD);

        renderPrevNextLink(writer, true, NEXT_DAY_CLASS, NEXT_DAY_EVENT);
        renderYearLinkAndLayout(writer, viewCalendar.get(Calendar.YEAR), layout, CHANGE_DAY_LAYOUT_EVENT, "Day");

        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        Map<Integer, String> dayName = new HashMap<>();
        dayName.put(viewCalendar.get(Calendar.DAY_OF_WEEK),
                viewCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
        viewCalendar.add(Calendar.DAY_OF_WEEK, 1);
        Date endTime = viewCalendar.getTime();
        renderDaysInWeek(writer, holder, viewCalendar, startTime, endTime, dayName, -1, layout);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }
}
