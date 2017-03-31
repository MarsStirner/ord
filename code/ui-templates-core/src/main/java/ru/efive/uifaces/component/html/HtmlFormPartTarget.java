package ru.efive.uifaces.component.html;

import ru.efive.uifaces.component.ComponentFamily;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 * Component provides <code>formPart</code> that should be processed by enclosed UICommand component.
 *
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.FormPartTarget")
public class HtmlFormPartTarget extends UIComponentBase {

    public HtmlFormPartTarget() {
        super();
        setRendererType(null);
    }

    // ----------------------------------------------------------------------------------------------------------------

    @Override
    public String getFamily() {
        return ComponentFamily.FORM_PART;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public String getFormPart() {
        return (String) getStateHelper().get(PropertyKeys.FORM_PART);
    }

    public void setFormPart(String formPart) {
        getStateHelper().put(PropertyKeys.FORM_PART, formPart);
    }

    private enum PropertyKeys {
        FORM_PART
    }

}
