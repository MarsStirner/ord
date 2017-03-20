package ru.efive.wf.core.data.impl;

import org.apache.commons.lang.StringUtils;
import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

public class InputReasonForm implements LocalBackingBean {

    @Override
    public String getForm() {
        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
                + "xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:e5ui=\"http://efive.ru/uitemplates\">\n"
                + "<div style=\"margin-top:0;\">\n"
                + "  <span class=\"title\">Комментарий:</span>\n"
                + "  <h:inputTextarea id=\"action_comment\"  rows=\"3\""
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
            if (StringUtils.isEmpty(actionCommentary)) {
                result.setProcessed(false);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Не указан комментарий", ""));
            } else {
                properties = new ArrayList<>();
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