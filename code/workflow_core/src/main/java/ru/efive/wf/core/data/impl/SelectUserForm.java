package ru.efive.wf.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import ru.entity.model.user.User;
import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;

public class SelectUserForm implements LocalBackingBean {

    @Override
    public String getForm() {
        return "<?xml version='1.0' encoding='UTF-8' ?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
                + "xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:e5ui=\"http://efive.ru/uitemplates\" "
                + "xmlns:e5ui-comp=\"http://efive.ru/uitemplates/composite\">\n"
                + "<div id=\"title\">Дата</div>\n"
                + "<div style=\"margin-top:0;\">\n"
                + "  <div class=\"searchbar\">\n"
                + "    <span style=\"float: left; margin-top: 3px\">Поиск:&nbsp;</span>\n"
                + "    <h:inputText id=\"filter_string\" value=\"#{" + getUserListBeanName() + ".filter}\" style=\"display:block; float:left; margin-right:10px;\" title=\"Поиск\" />\n"
                + "    <h:commandButton value=\" \" action=\"#{" + getUserListBeanName() + ".changePageOffset(0)}\" styleClass=\"searchbutton\">\n"
                + "      <f:ajax execute=\":main_content_form:actionsModal:filter_string\"\n"
                + "            render=\":main_content_form:actionsModal:ajaxModal :main_content_form:actionsModal:modal_paging\" />\n"
                + "    </h:commandButton>"
                + "  </div>\n"
                + "  <div class=\"wrap\" style=\"height: 300px;\">\n"
                + "    <div class=\"inner\" style=\"height: 300px;\">\n"
                + "      <e5ui:dataTable id=\"select_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" value=\"#{" + getUserListBeanName() + ".documents}\"\n"
                + "            var=\"row\" grouping=\"false\" width=\"100%\">"
                + "        <e5ui:row onclick=\"e5ui_util.clickElement(this, 'selectLink$');\" styleClass=\"#{" + getBeanName()
                + ".processorModal.processedActivity.document.selected(row)? 'grid_row_selected': ''}\" />\n"
                + "        <e5ui:column>\n"
                + "          <f:facet name=\"header\">\n"
                + "            <p>Сотрудник</p>\n"
                + "          </f:facet>\n"
                + "          <h:outputText value=\"#{row}\" converter=\"PersonConverter\" />\n"
                + "          <h:commandLink id=\"selectLink\" style=\"display: none;\" rendered=\"#{not " + getBeanName() + ".processorModal.processedActivity.document.selected(row)}\"\n"
                + "                action=\"#{" + getBeanName() + ".processorModal.processedActivity.document.setSelectedUser(row)}\" />\n"
                + "        </e5ui:column>\n"
                + "      </e5ui:dataTable>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div style=\"clear: both;\">\n"
                + "    <h:panelGroup id=\"modal_paging\">\n"
                + "      <e5ui-comp:tablePager documentListHolder=\"#{" + getUserListBeanName() + "}\" />\n"
                + "    </h:panelGroup>\n"
                + "  </div>\n"
                + "</div>\n"
                + "</html>";
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public boolean selected(User user) {
        return selectedUser == null ? false : selectedUser.equals(user);
    }

    public void setScope(EditablePropertyScope scope) {
        this.scope = scope;
    }

    public EditablePropertyScope getScope() {
        return scope;
    }

    public String getSelectedUserField() {
        return selectedUserField;
    }

    public void setSelectedUserField(String selectedUserField) {
        this.selectedUserField = selectedUserField;
    }

    @Override
    public List<EditableProperty> getProperties() {
        return properties == null ? new ArrayList<EditableProperty>() : properties;
    }

    @Override
    public ActionResult initialize() {
        ActionResult result = new ActionResult();
        try {
            if (selectedUser == null) {
                result.setProcessed(false);
                result.setDescription("Не выбран сотрудник");
            } else {
                properties = new ArrayList<EditableProperty>();
                properties.add(new EditableProperty(getSelectedUserField(), getSelectedUser(), getScope()));
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

    public String getUserListBeanName() {
        return userListBeanName;
    }

    public void setUserListBeanName(String userListBeanName) {
        this.userListBeanName = userListBeanName;
    }


    private List<EditableProperty> properties;

    private String beanName;
    private String userListBeanName;

    private User selectedUser;
    private EditablePropertyScope scope;
    private String selectedUserField;
}