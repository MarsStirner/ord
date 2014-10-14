package ru.efive.wf.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import ru.entity.model.user.UserAccessLevel;
import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;

public class SelectAccessLevelForm implements LocalBackingBean {

    @Override
    public String getForm() {
        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
                + "xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:e5ui=\"http://efive.ru/uitemplates\" "
                + "xmlns:e5ui-comp=\"http://efive.ru/uitemplates/composite\">\n"
                + "<div id=\"title\">Выбор уровня допуска</div>\n"
                + "<div style=\"margin-top:0;\">\n"
                + "  <div class=\"wrap\" style=\"height:300px;\">\n"
                + "    <div class=\"inner\" style=\"height:300px;\">\n"
                + "      <e5ui:dataTable id=\"select_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" value=\"#{dictionaryManagement.getUserAccessLevelsGreaterOrEqualMaxValue(sessionManagement.getLoggedUser().getCurrentUserAccessLevel().level)}\"\n"
                + "            var=\"row\" grouping=\"false\" width=\"100%\">"
                + "        <e5ui:row onclick=\"e5ui_util.clickElement(this, 'selectLink$');\" styleClass=\"#{" + getBeanName()
                + ".processorModal.processedActivity.document.selected(row)? 'grid_row_selected': ''}\" />\n"
                + "        <e5ui:column>\n"
                + "          <f:facet name=\"header\">\n"
                + "            <p>Уровень допуска</p>\n"
                + "          </f:facet>\n"
                + "          <h:outputText value=\"#{row}\" converter=\"UserAccessLevelConverter\" />\n"
                + "          <h:commandLink id=\"selectLink\" style=\"display: none;\" rendered=\"#{not " + getBeanName() + ".processorModal.processedActivity.document.selected(row)}\"\n"
                + "                action=\"#{" + getBeanName() + ".processorModal.processedActivity.document.setSelectedAccessLevel(row)}\" />\n"
                + "        </e5ui:column>\n"
                + "      </e5ui:dataTable>\n"
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
                properties = new ArrayList<EditableProperty>();
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