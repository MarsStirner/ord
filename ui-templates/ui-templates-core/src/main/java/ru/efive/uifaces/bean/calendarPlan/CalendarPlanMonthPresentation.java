package ru.efive.uifaces.bean.calendarPlan;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CAPTION_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.LAYOUT_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_MONTH_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_MONTH_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.MONTH_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.MONTH_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.YEAR_NAME_CLASS;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_MONTH_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_MONTH_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.NEXT_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.PREV_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.SELECT_YEAR_EVENT;
import static ru.efive.uifaces.renderkit.html_basic.CalendarPlanRenderer.CHANGE_MONTH_LAYOUT_EVENT;

import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.getDisplayNames;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.DAY_OF_WEEK_NAMES;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.MONTH_NAMES;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderMonthWidget;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderLayoutLink;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderSpaceCell;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderPrevNextLink;
import static ru.efive.uifaces.bean.calendarPlan.CalendarPlanYearPresentation.renderSelectLink;


/**
 *
 * @author Pavel Porubov
 */
public class CalendarPlanMonthPresentation extends CalendarPlanPresentation {

    @Override
    public String getName() {
        return "month";
    }

    public static final int ID = 2;

    @Override
    public int getId() {
        return ID;
    }

    public static enum Layout {
        one, vertical3, horizontal3
    }
    private Layout layout = Layout.one;

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    private static void renderTableTrStart(AdvancedResponseWriter writer, boolean caption) throws IOException {
        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(null, writer, caption ? CAPTION_CLASS : LAYOUT_CLASS, MONTH_CLASS);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);
    }

    private static void renderTableTrEnd(AdvancedResponseWriter writer) throws IOException {
        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }

    @Override
    public void render(AdvancedResponseWriter writer, CalendarPlanHolder holder) throws IOException {
        writer.startElement(HtmlElement.TABLE);
        writer.startElement(HtmlElement.TBODY);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        Locale locale = writer.getContext().getViewRoot().getLocale();
        Calendar calendar = (Calendar) holder.getViewCalendar().clone();
        Map<Integer, String>[] names = getDisplayNames(calendar, locale);
        int month = calendar.get(Calendar.MONTH);

        renderTableTrStart(writer, true);
        renderSpaceCell(writer);

        if (layout == Layout.one) {
            renderPrevNextLink(writer, false, PREV_MONTH_CLASS, PREV_MONTH_EVENT);
            renderSelectLink(writer, names[MONTH_NAMES].get(month), MONTH_NAME_CLASS, null);
            renderPrevNextLink(writer, true, NEXT_MONTH_CLASS, NEXT_MONTH_EVENT);
            renderSpaceCell(writer);
        }

        renderPrevNextLink(writer, false, PREV_YEAR_CLASS, PREV_YEAR_EVENT);
        String year = Integer.toString(calendar.get(Calendar.YEAR));
        renderSelectLink(writer, year, YEAR_NAME_CLASS, SELECT_YEAR_EVENT, year);
        renderPrevNextLink(writer, true, NEXT_YEAR_CLASS, NEXT_YEAR_EVENT);
        renderSpaceCell(writer);
        renderLayoutLink(writer, layout, CHANGE_MONTH_LAYOUT_EVENT, "Month");
        renderTableTrEnd(writer);

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TR);
        writer.startElement(HtmlElement.TD);

        int firstMonth = month == Calendar.JANUARY ? Calendar.DECEMBER : month - 1;
        int lastMonth = month == Calendar.DECEMBER ? Calendar.JANUARY : month + 1;
        int m;
        switch (layout) {
            case one:
                renderMonthWidget(writer, holder, calendar, month, false, names[DAY_OF_WEEK_NAMES]);
                break;
            case horizontal3:
                renderTableTrStart(writer, false);
                m = firstMonth;
                while (true) {
                    writer.startElement(HtmlElement.TD);
                    renderTableTrStart(writer, true);
                    renderSpaceCell(writer);
                    if (m == firstMonth) {
                        renderPrevNextLink(writer, false, PREV_MONTH_CLASS, PREV_MONTH_EVENT);
                    }
                    renderSelectLink(writer, names[MONTH_NAMES].get(m), MONTH_NAME_CLASS, null);
                    if (m == lastMonth) {
                        renderPrevNextLink(writer, true, NEXT_MONTH_CLASS, NEXT_MONTH_EVENT);
                    }
                    renderSpaceCell(writer);
                    renderTableTrEnd(writer);
                    writer.endElement(HtmlElement.TD);
                    if (m == lastMonth) {
                        break;
                    } else {
                        m = m == Calendar.DECEMBER ? Calendar.JANUARY : m + 1;
                    }
                }
                writer.endElement(HtmlElement.TR);
                writer.startElement(HtmlElement.TR);
                m = firstMonth;
                while (true) {
                    writer.startElement(HtmlElement.TD);
                    renderMonthWidget(writer, holder, calendar, m, false, names[DAY_OF_WEEK_NAMES]);
                    writer.endElement(HtmlElement.TD);
                    if (m == lastMonth) {
                        break;
                    } else {
                        m = m == Calendar.DECEMBER ? Calendar.JANUARY : m + 1;
                    }
                }
                renderTableTrEnd(writer);
                break;
            case vertical3:
                renderTableTrStart(writer, false);
                m = firstMonth;
                while (true) {
                    writer.startElement(HtmlElement.TD);
                    renderTableTrStart(writer, true);
                    renderSpaceCell(writer);
                    if (m == month) {
                        renderPrevNextLink(writer, false, PREV_MONTH_CLASS, PREV_MONTH_EVENT);
                    }
                    renderSelectLink(writer, names[MONTH_NAMES].get(m), MONTH_NAME_CLASS, null);
                    if (m == month) {
                        renderPrevNextLink(writer, true, NEXT_MONTH_CLASS, NEXT_MONTH_EVENT);
                    }
                    renderSpaceCell(writer);
                    renderTableTrEnd(writer);
                    writer.endElement(HtmlElement.TD);
                    writer.endElement(HtmlElement.TR);

                    writer.startElement(HtmlElement.TR);
                    writer.startElement(HtmlElement.TD);
                    renderMonthWidget(writer, holder, calendar, m, false, names[DAY_OF_WEEK_NAMES]);
                    writer.endElement(HtmlElement.TD);

                    if (m == lastMonth) {
                        break;
                    } else {
                        m = m == Calendar.DECEMBER ? Calendar.JANUARY : m + 1;
                    }

                    writer.endElement(HtmlElement.TR);
                }
                renderTableTrEnd(writer);
                break;
        }

        writer.endElement(HtmlElement.TD);
        writer.endElement(HtmlElement.TR);
        writer.endElement(HtmlElement.TBODY);
        writer.endElement(HtmlElement.TABLE);
    }
}
