package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.behavior.ClientBehaviorHolder;
import ru.efive.uifaces.bean.calendarPlan.CalendarPlanHolder;
import ru.efive.uifaces.renderkit.html_basic.FileUploadRenderer;

/**
 * The component represents a widget to display and operate with calendar tasks and events.
 *
 * @author Pavel Porubov
 */
@FacesComponent(CalendarPlan.COMPONENT)
public class CalendarPlan extends UIComponentBase implements ClientBehaviorHolder {

    /** Component name */
    public static final String COMPONENT = "ru.efive.uifaces.CalendarPlan";

    /** Default constructor. */
    public CalendarPlan() {
        setRendererType(FileUploadRenderer.RENDERER);
    }

    /** {@inheritDoc} */
    @Override
    public String getFamily() {
        return COMPONENT;
    }

    /**
     * Component properties.
     * <br>There are following properties:
     * <ul>
     * <li>{@code holder} - stores a bean which holds the state of component.</li>
     * </ul>
     */
    public enum PropertyKeys {
        holder
    }

    /**
     * Returns the value of {@code holder} property.
     * @return value of {@code holder} property
     * @see PropertyKeys
     */
    public CalendarPlanHolder getHolder() {
        return (CalendarPlanHolder) getStateHelper().eval(PropertyKeys.holder);
    }

    /**
     * Sets the value of property {@code holder}.
     * @param maxFilesCount value of {@code holder} property
     * @see PropertyKeys
     */
    public void setHolder(CalendarPlanHolder holder) {
        if (holder != null) {
            getStateHelper().put(PropertyKeys.holder, holder);
        } else {
            getStateHelper().remove(PropertyKeys.holder);
        }
    }
}
