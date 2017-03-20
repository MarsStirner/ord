package ru.efive.wf.core.data.impl;

import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;
import ru.entity.model.referenceBook.UserAccessLevel;

import java.util.ArrayList;
import java.util.List;

public class SelectAccessLevelForm implements LocalBackingBean {

    @Override
    public String getForm() {
        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
                + "xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:e5ui=\"http://efive.ru/uitemplates\"  xmlns:p=\"http://primefaces.org/ui\" "
                + "xmlns:e5ui-comp=\"http://efive.ru/uitemplates/composite\">\n"
                + "<div id=\"title\">Выбор уровня допуска</div>\n"
                + "<div style=\"margin-top:0;\">\n"
                + "  <div class=\"wrap\" style=\"height:200px;\">\n"
                + "    <div class=\"inner\" style=\"height:200px;\">\n"
                + "      <p:dataTable id=\"select_table\" value=\"#{dictionaryManagement.getUserAccessLevelsGreaterOrEqualMaxValue(sessionManagement.getAuthData().getCurrentAccessLevel().level)}\"\n"
                + "            var=\"row\" style=\"width:100%\" selectionMode=\"single\" selection=\"#{" + getBeanName() + ".processorModal.processedActivity.document.selectedAccessLevel}\" rowKey=\"#{row.level}\">"
                + "        <p:column headerText=\"Уровень допуска\">"
                + "          <h:outputText value=\"#{row.value}\"/>\n"
                + "        </p:column>\n"
                + "      </p:dataTable>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</div>\n"
                + "</html>";
    }

    public UserAccessLevel getSelectedAccessLevel() {
        return selectedAccessLevel;
    }

    public void setSelectedAccessLevel(UserAccessLevel selectedAccessLevel) {
        this.selectedAccessLevel = selectedAccessLevel;
    }

    public boolean selected(UserAccessLevel selectedAccessLevel) {
        return this.selectedAccessLevel == null ? false : this.selectedAccessLevel.equals(selectedAccessLevel);
    }

    public void setScope(EditablePropertyScope scope) {
        this.scope = scope;
    }

    public EditablePropertyScope getScope() {
        return scope;
    }

    public String getSelectedAccessLevelField() {
        return selectedAccessLevelField;
    }

    public void setSelectedAccessLevelField(String selectedAccessLevelField) {
        this.selectedAccessLevelField = selectedAccessLevelField;
    }

    @Override
    public List<EditableProperty> getProperties() {
        return properties == null ? new ArrayList<EditableProperty>() : properties;
    }

    @Override
    public ActionResult initialize() {
        ActionResult result = new ActionResult();
        try {
            if (selectedAccessLevel == null) {
                result.setProcessed(false);
                result.setDescription("Не выбран уровень допуска");
            } else {
                properties = new ArrayList<>();
                properties.add(new EditableProperty(getSelectedAccessLevelField(), getSelectedAccessLevel(), getScope()));
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

    private UserAccessLevel selectedAccessLevel;
    private EditablePropertyScope scope;
    private String selectedAccessLevelField;
}