package ru.efive.uifaces.bean.calendarPlan;

import java.io.IOException;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;

/**
 *
 * @author Pavel Porubov
 */
public abstract class CalendarPlanPresentation {

    public abstract String getName();
    public abstract int getId();
    public abstract void render(AdvancedResponseWriter writer, CalendarPlanHolder holder) throws IOException;
}
