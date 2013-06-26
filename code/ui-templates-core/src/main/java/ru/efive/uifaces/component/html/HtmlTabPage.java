package ru.efive.uifaces.component.html;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.behavior.ClientBehaviorHolder;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.renderkit.html_basic.HtmlTabPanelRenderer;

/**
 * The tab page component. Used with {@link HtmlTabPanel}. Represents content page and it's header.
 *
 * @author Pavel Porubov
 */
@FacesComponent(HtmlTabPage.COMPONENT)
public class HtmlTabPage extends UICommand implements ClientBehaviorHolder {

    /** Component name */
    public static final String COMPONENT = "ru.efive.uifaces.TabPage";

    /** Default constructor. */
    public HtmlTabPage() {
        setRendererType(HtmlTabPanelRenderer.RENDERER);
    }

    /** {@inheritDoc} */
    @Override
    public String getFamily() {
        return ComponentFamily.TAB_PANEL;
    }
    
    /** Facet name for header part */
    public static final String HEADER_FACET = "header";

    /**
     * Component properties.
     * <br>There are following properties:
     * <ul>
     * <li>{@code headerText} - represents the header text. The header also could be produced by specifying facet with
     * name {@link HtmlTabPage#HEADER_FACET}. For such case the header of page would be filled with content of that facet.
     * Otherwise if {@code headerText} property has been set then the header would be filled with simple text.
     * Specifying header by facet is more priority.</li>
     * <li>{@code selected} - used for indicating that a page is selected.
     * Each tab page can be selected if it is not disabled before.</li>
     * <li>{@code actionBehavior} - determines the components's behavior when it is activated.</li>
     * <li>{@code disabled} - used for indicating that a page is disabled.
     * Each tab page can be disabled so it can not be selected.</li>
     * </ul>
     * @see ActionBehavior
     */
    public enum PropertyKeys {
        headerText,
        selected,
        actionBehavior,
        disabled
    }
    
    /**
     * Returns {@code headerText} property if it specified otherwise null.
     * @return header text
     * @see PropertyKeys
     */
    public String getHeaderText() {
        return (String) getStateHelper().eval(PropertyKeys.headerText);
    }

    /**
     * Sets the value of {@code headerText} property.
     * @param headerText value of {@code headerText} property
     * @see PropertyKeys
     */
    public void setHeaderText(String headerText) {
        getStateHelper().put(PropertyKeys.headerText, headerText);
    }

    /**
     * Returns the value of {@code selected} property.
     * @return {@code true} if the page is selected, otherwise {@code false}.
     * @see PropertyKeys
     */
    public boolean isSelected() {
        return (Boolean) getStateHelper().eval(PropertyKeys.selected, false);

    }

    /**
     * Sets the value of property {@code selected}.
     * @param selected value of {@code selected} property
     * @see PropertyKeys
     */
    public void setSelected(boolean selected) {
        getStateHelper().put(PropertyKeys.selected, selected);
    }

    /**
     * Returns the value of {@code disabled} property.
     * @return {@code true} if the page is disabled, otherwise {@code false}.
     * @see PropertyKeys
     */
    public boolean isDisabled() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);

    }

    /**
     * Sets the value of property {@code disabled}.
     * @param disabled value of {@code disabled} property
     * @see PropertyKeys
     */
    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    /**
     * The variants of components's behavior when it is activated or by other words when a tab page is selected.
     * <br>There are following values:
     * <ul>
     * <li>{@code client} - the tab page is switched to selected at the browser. No requests is sent to server.
     * The content of such tab pages rendered once and simply displayed each time when the tab page is selected.</li>
     * <li>{@code submit} - switching to such tab page causes the full page refresh. The content of such tab page is
     * rendered only when it is in selected state.</li>
     * <li>{@code ajax} - switching to such tab page causes ajax refresh of only that tab page.
     * The content of such tab page is rendered only when it is in selected state.</li>
     * </ul>
     * The default behavior is {@code client}.
     */
    public enum ActionBehavior {
        client, submit, ajax
    }

    /**
     * Returns the value of {@code actionBehavior} property.
     * @return value of {@code actionBehavior} property
     * @see PropertyKeys
     * @see ActionBehavior
     */
    public ActionBehavior getActionBehavior() {
        return (ActionBehavior) getStateHelper().eval(PropertyKeys.actionBehavior, ActionBehavior.client);
    }

    /**
     * Sets the value of property {@code actionBehavior}.
     * @param actionBehavior value of {@code actionBehavior} property
     * @see PropertyKeys
     * @see ActionBehavior
     */
    public void setActionBehavior(ActionBehavior actionBehavior) {
        getStateHelper().put(PropertyKeys.actionBehavior, actionBehavior);
    }
    
    /** The name of default and only event of tab page. */
    public static final String DEFAULT_EVENT_NAME = "select";

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(
            Arrays.asList(DEFAULT_EVENT_NAME));

    /** {@inheritDoc} */
    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT_NAME;
    }
}
