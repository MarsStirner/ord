package ru.efive.wf.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

//import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;

public class InputRegistrationNumberForm implements LocalBackingBean {

    @Override
    public String getForm() {
        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
                + "xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:e5ui=\"http://efive.ru/uitemplates\">\n"
                + "<div id=\"title\">Укажите номер приказа</div>\n"
                + "<div style=\"margin-top:0;\">\n"
                + "  <span class=\"title\">Номер приказа:</span>\n"
                + "  <h:inputTextarea id=\"action_comment\"  rows=\"3\" rendered=\"true\""
                + " immediate=\"true\" value=\"#{" + getBeanName()
                + ".processorModal.processedActivity.document.actionCommentary}\" style=\"width:98%;\"/>\n"
                + "</div>\n"
                + "</html>";
    }

    public void setActionCommentary(String actionCommentary) {
        this.actionCommentary = actionCommentary;
    }

    public String getActionCommentary() {
        return actionCommentary;
    }

    public void setScope(EditablePropertyScope scope) {
        this.scope = scope;
    }

    public EditablePropertyScope getScope() {
        return scope;
    }

    public void setActionCommentaryField(String actionCommentaryField) {
        this.actionCommentaryField = actionCommentaryField;
    }

    public String getActionCommentaryField() {
        return actionCommentaryField;
    }

    @Override
    public List<EditableProperty> getProperties() {
        return properties == null ? new ArrayList<EditableProperty>() : properties;
    }

    @Override
    public ActionResult initialize() {
        ActionResult result = new ActionResult();
        try {
            if (actionCommentary == null || actionCommentary.equals("")) {
                result.setProcessed(false);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Не указан номер", ""));
            } else {
                properties = new ArrayList<EditableProperty>();
                properties.add(new EditableProperty(getActionCommentaryField(), getActionCommentary(), getScope()));
                result.setProcessed(true);
            }
        } catch (Exception e) {
            result.setProcessed(false);
            result.setDescription("Exception in initialize");
            e.printStackTrace();
        }
        return result;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }


    private List<EditableProperty> properties;

    private String beanName;

    private String actionCommentary;
    private EditablePropertyScope scope;
    private String actionCommentaryField;
}