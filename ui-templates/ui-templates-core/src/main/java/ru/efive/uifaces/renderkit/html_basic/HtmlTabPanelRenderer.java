package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlTabPage;
import ru.efive.uifaces.component.html.HtmlTabPage.ActionBehavior;
import ru.efive.uifaces.component.html.HtmlTabPanel;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttributeValue;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static java.lang.String.format;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyle;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.findEnclosingForm;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.updateComponentAttribute;

/**
 * The renderer for output {@link HtmlTabPanel} and {@link HtmlTabPage}.
 *
 * @author Pavel Porubov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = HtmlTabPanelRenderer.RENDERER,
        componentFamily = ComponentFamily.TAB_PANEL)
@ResourceDependencies({
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "tabPanel.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "tabPanel.css", target = "head", library = "e5ui/css")})
public class HtmlTabPanelRenderer extends HtmlBasicRenderer {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER = "ru.efive.uifaces.TabPanel";

    private static final String WRONG_COMPONENT = "Can not render component of class %s";

    /** The surrounding div's class. */
    public static final String TAB_PANEL_CLASS = "e5uiTabPanel";
    /** The header's div's class. */
    public static final String TAB_PANEL_HEADER_CLASS = "e5uiTabPanelHeader";
    /** The page button's div's class. */
    public static final String TAB_PANEL_HEADER_PAGE_CLASS = "e5uiTabPanelHeaderPage";
    /** The content's div's class. */
    public static final String TAB_PANEL_CONTENT_CLASS = "e5uiTabPanelContent";
    /** The content page's div's class. */
    public static final String TAB_PANEL_CONTENT_PAGE_CLASS = "e5uiTabPanelContentPage";
    /** The marker class that assumes selected page. */
    public static final String TAB_PANEL_SELECTED_PAGE_CLASS = "selected";
    /** The marker class that assumes disabled page. */
    public static final String TAB_PANEL_DISABLED_PAGE_CLASS = "disabled";

    /** The header's div's id suffix. */
    public static final String TAB_PANEL_HEADER_ID_SUFFIX = "-header";
    /** The content's div's id suffix. */
    public static final String TAB_PANEL_PAGE_ID_SUFFIX = "-content";
    
    private static final String TAB_PANEL_ONCLICK = "e5ui_tabPanel.selectTab(this);";
    private static final String TAB_PANEL_SET_SELECTED_TAB = "document.getElementById('%s').value='%s';";
    private static final String FORM_BY_ID_SUBMIT = "document.getElementById('%s').submit();";

    private static String makeSelectedId(HtmlTabPanel tabPanel) {
        return tabPanel.getClientId() + "-" + HtmlTabPage.PropertyKeys.selected.name();
    }

    private static void updateSelectedAttribute(HtmlTabPage tabPage, boolean selected) {
        updateComponentAttribute(FacesContext.getCurrentInstance(),
                tabPage, HtmlTabPage.PropertyKeys.selected.name(), selected);
    }

    private static int[] findSelectedTabPage(HtmlTabPage component) {
        int currentIndex = -1, selectedIndex = -1, firstIndex = -1;
        UIComponent parent = component.getParent();
        if (parent instanceof HtmlTabPanel) {
            List<UIComponent> children = parent.getChildren();
            if (children != null && !children.isEmpty()) {
                for (int childIndex = 0; childIndex < children.size()
                        || (currentIndex < 0 && selectedIndex < 0); childIndex++) {
                    UIComponent childComponent = children.get(childIndex);
                    if (component == childComponent) {
                        currentIndex = childIndex;
                    }
                    if (selectedIndex < 0 && childComponent instanceof HtmlTabPage && childComponent.isRendered()) {
                        if (firstIndex < 0) {
                            firstIndex = childIndex;
                        }
                        if (((HtmlTabPage) childComponent).isSelected()) {
                            selectedIndex = childIndex;
                        }
                    }
                }
            }
        }
        return new int[]{currentIndex, (selectedIndex < 0 ? firstIndex : selectedIndex)};
    }

    private static boolean isTabPageSelected(HtmlTabPage component) {
        int[] tabPageIndex = findSelectedTabPage(component);
        return tabPageIndex[0] == tabPageIndex[1];
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        UIComponent component = writer.getComponent();
        if (!component.isRendered()) {
            return;
        }

        if (component instanceof HtmlTabPanel) {
            //frame
            writer.startElement(HtmlElement.DIV);
            writer.writeAttribute(HtmlAttribute.ID, component.getClientId(), null);
            writeStyleClass(component, writer, TAB_PANEL_CLASS);
            writeStyle(component, writer);

            //header frame
            writer.startElement(HtmlElement.DIV);
            writer.writeAttribute(HtmlAttribute.ID, component.getClientId() + TAB_PANEL_HEADER_ID_SUFFIX, null);
            writer.writeAttribute(HtmlAttribute.CLASS, TAB_PANEL_HEADER_CLASS, null);

            List<UIComponent> children = component.getChildren();
            if (children != null && !children.isEmpty()) {
                boolean hasSelectedParameters = false;
                int selectedIndex = -1, firstIndex = -1;
                for (int childIndex = 0; childIndex < children.size(); childIndex++) {
                    UIComponent childComponent = children.get(childIndex);
                    if (childComponent instanceof HtmlTabPage && childComponent.isRendered()) {
                        if (firstIndex < 0) {
                            firstIndex = childIndex;
                        }
                        HtmlTabPage tabPage = (HtmlTabPage) childComponent;
                        if (selectedIndex < 0 && tabPage.isSelected()) {
                            selectedIndex = childIndex;
                        }
                        switch (tabPage.getActionBehavior()) {
                            case submit:
                            case ajax:
                                hasSelectedParameters = true;
                        }
                    }
                }
                if (selectedIndex < 0) {
                    selectedIndex = firstIndex;
                }

                String selectedParameterId = makeSelectedId((HtmlTabPanel) component);
                if (hasSelectedParameters) {
                    writer.startElement(HtmlElement.INPUT);
                    writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
                    writer.writeAttribute(HtmlAttribute.ID, selectedParameterId, null);
                    writer.writeAttribute(HtmlAttribute.NAME, selectedParameterId, null);
                    writer.writeAttribute(HtmlAttribute.VALUE, children.get(selectedIndex).getClientId(), null);
                    writer.endElement(HtmlElement.INPUT);
                }

                for (int childIndex = 0; childIndex < children.size(); childIndex++) {
                    UIComponent childComponent = children.get(childIndex);
                    if (childComponent instanceof HtmlTabPage && childComponent.isRendered()) {
                        HtmlTabPage tabPage = (HtmlTabPage) childComponent;

                        boolean selected = childIndex == selectedIndex;
                        updateSelectedAttribute(tabPage, selected);
                        boolean disabled = tabPage.isDisabled();

                        writer.startElement(HtmlElement.SPAN);
                        writer.writeAttribute(HtmlAttribute.ID, component.getClientId() + TAB_PANEL_HEADER_ID_SUFFIX
                                + "-" + childIndex, null);
                        writer.writeAttribute(HtmlAttribute.CLASS, TAB_PANEL_HEADER_PAGE_CLASS
                                + (selected ? " " + TAB_PANEL_SELECTED_PAGE_CLASS : "")
                                + (disabled ? " " + TAB_PANEL_DISABLED_PAGE_CLASS : ""), null);

                        if (!disabled) {
                            StringBuilder onClickHandler = hasSelectedParameters
                                    ? new StringBuilder(format(TAB_PANEL_SET_SELECTED_TAB,
                                    selectedParameterId, tabPage.getClientId()))
                                    : new StringBuilder();
                            switch (tabPage.getActionBehavior()) {
                                case submit:
                                    Map<String, List<ClientBehavior>> cbm = tabPage.getClientBehaviors();
                                    if (cbm != null && !cbm.isEmpty()) {
                                        List<ClientBehavior> cbl = cbm.get(HtmlTabPage.DEFAULT_EVENT_NAME);
                                        if (cbl != null && !cbl.isEmpty()) {
                                            ClientBehaviorContext cbCtx =
                                                    ClientBehaviorContext.createClientBehaviorContext(
                                                    writer.getContext(), tabPage, HtmlTabPage.DEFAULT_EVENT_NAME,
                                                    null, null);
                                            for (ClientBehavior cb : cbl) {
                                                if (cb instanceof AjaxBehavior) {
                                                    String cbs = cb.getScript(cbCtx);
                                                    if (cbs != null && !cbs.isEmpty()) {
                                                        onClickHandler.append(cbs).append(";");
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        onClickHandler.append(
                                                format(FORM_BY_ID_SUBMIT, findEnclosingForm(tabPage).getClientId()));
                                    }
                                    break;
                                case ajax:
                                    findEnclosingForm(tabPage);
                                    AjaxBehavior ab = new AjaxBehavior();
                                    List<String> ids = Arrays.asList(component.getClientId());
                                    ab.setExecute(ids);
                                    ab.setRender(ids);
                                    ClientBehaviorContext abCtx = ClientBehaviorContext.createClientBehaviorContext(
                                            writer.getContext(), tabPage, HtmlTabPage.DEFAULT_EVENT_NAME, null, null);
                                    onClickHandler.append(ab.getScript(abCtx)).append(";");
                                    break;
                                case client:
                                    onClickHandler.append(TAB_PANEL_ONCLICK);
                                    break;
                            }
                            writer.writeAttribute(HtmlAttribute.ONCLICK, onClickHandler.toString(), null);
                        }

                        if (!writer.writeFacet(HtmlTabPage.HEADER_FACET, tabPage)) {
                            String headerText = tabPage.getHeaderText();
                            if (headerText != null) {
                                writer.write(headerText);
                            } else {
                                writer.write("&nbsp;");
                            }
                        }

                        writer.endElement(HtmlElement.SPAN);
                    }
                }
            }

            writer.endElement(HtmlElement.DIV); //header frame

            //pages frame
            writer.startElement(HtmlElement.DIV);
            writer.writeAttribute(HtmlAttribute.ID, component.getClientId() + TAB_PANEL_PAGE_ID_SUFFIX, null);
            writer.writeAttribute(HtmlAttribute.CLASS, TAB_PANEL_CONTENT_CLASS, null);
        } else if (component instanceof HtmlTabPage) {
            if (component.getParent() instanceof HtmlTabPanel && component.isRendered()) {
                HtmlTabPage tabPage = (HtmlTabPage) component;
                int[] tabPageIndex = findSelectedTabPage(tabPage);
                boolean selected = tabPageIndex[0] == tabPageIndex[1];
                if (selected || tabPage.getActionBehavior() == ActionBehavior.client) {
                    //page frame
                    writer.startElement(HtmlElement.DIV);
                    writer.writeAttribute(HtmlAttribute.ID, component.getParent().getClientId()
                            + TAB_PANEL_PAGE_ID_SUFFIX
                            + "-" + tabPageIndex[0], null);
                    writeStyleClass(tabPage, writer, TAB_PANEL_CONTENT_PAGE_CLASS,
                            (selected ? TAB_PANEL_SELECTED_PAGE_CLASS : null));
                    writeStyle(tabPage, writer);
                }
            }
        } else {
            throw new IllegalArgumentException(format(WRONG_COMPONENT, component.getClass().getName()));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeChildren(AdvancedResponseWriter writer,
            List<UIComponent> children) throws IOException {
        UIComponent component = writer.getComponent();
        if (!component.isRendered()) {
            return;
        }

        if (component instanceof HtmlTabPanel) {
            for (UIComponent childComponent : children) {
                if (childComponent instanceof HtmlTabPage) {
                    childComponent.encodeAll(writer.getContext());
                }
            }
        } else if (component instanceof HtmlTabPage) {
            HtmlTabPage tabPage = (HtmlTabPage) component;
            if (component.getParent() instanceof HtmlTabPanel
                    && (tabPage.getActionBehavior() == ActionBehavior.client || isTabPageSelected(tabPage))) {
                super.encodeChildren(writer, children);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        UIComponent component = writer.getComponent();
        if (!component.isRendered()) {
            return;
        }

        if (component instanceof HtmlTabPanel) {
            writer.endElement(HtmlElement.DIV); //pages frame
            writer.endElement(HtmlElement.DIV); //frame
        } else if (component instanceof HtmlTabPage) {
            HtmlTabPage tabPage = (HtmlTabPage) component;
            if (component.getParent() instanceof HtmlTabPanel
                    && (tabPage.getActionBehavior() == ActionBehavior.client || isTabPageSelected(tabPage))) {
                writer.endElement(HtmlElement.DIV); //page frame
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        super.decode(context, component);
        if (component instanceof HtmlTabPanel) {
            HtmlTabPanel tabPanel = (HtmlTabPanel) component;
            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
            String selectedParameter = requestParameterMap.get(makeSelectedId(tabPanel));
            if (selectedParameter != null) {
                for (UIComponent childComponent : tabPanel.getChildren()) {
                    if (childComponent instanceof HtmlTabPage) {
                        boolean selected = selectedParameter.equals(childComponent.getClientId());
                        updateSelectedAttribute((HtmlTabPage) childComponent, selected);
                        if (selected) {
                            ActionEvent actionEvent = new ActionEvent(childComponent);
                            childComponent.queueEvent(actionEvent);
                        }
                    }
                }
            }
        }
    }
}
