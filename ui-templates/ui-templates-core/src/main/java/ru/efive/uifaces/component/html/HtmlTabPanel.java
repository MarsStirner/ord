package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIPanel;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.renderkit.html_basic.HtmlTabPanelRenderer;

/**
 * The tab panel component. Used as container for {@link HtmlTabPage} conponents.
 *
 * @author Pavel Porubov
 */
@FacesComponent(HtmlTabPanel.COMPONENT)
public class HtmlTabPanel extends UIPanel {

    /** Component name */
    public static final String COMPONENT = "ru.efive.uifaces.TabPanel";

    /** Default constructor. */
    public HtmlTabPanel() {
        setRendererType(HtmlTabPanelRenderer.RENDERER);
    }

    /** {@inheritDoc} */
    @Override
    public String getFamily() {
        return ComponentFamily.TAB_PANEL;
    }
}
