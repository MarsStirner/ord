package ru.efive.dms.uifaces.beans;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.referenceBook.*;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("dictionaryManagement")
@SessionScoped
public class DictionaryManagementBean implements Serializable {

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();


    private MenuModel accessLevelsMenuModel;
    private String filter;

    public MenuModel getAccessLevelsMenuModel() {
        if (accessLevelsMenuModel == null) {
            accessLevelsMenuModel = new DefaultMenuModel();
            for (UserAccessLevel current : getUserAccessLevelsLowerOrEqualMaxValue(sessionManagement.getAuthData().getMaxAccessLevel().getLevel())) {
                final DefaultMenuItem currentItem = new DefaultMenuItem(current.getValue());
                currentItem.setCommand("#{sessionManagement.setCurrentUserAccessLevel(\'".concat(String.valueOf(current.getId())).concat("\')}"));
                currentItem.setUpdate("accessMenuButton accessMenu");
                accessLevelsMenuModel.addElement(currentItem);
            }
        }
        return accessLevelsMenuModel;
    }

    public List<UserAccessLevel> getUserAccessLevels() {
        return sessionManagement.getDictionaryDAO(UserAccessLevelDAOImpl.class, USER_ACCESS_LEVEL_DAO).findDocuments();
    }

    public List<UserAccessLevel> getUserAccessLevelsLowerOrEqualMaxValue(int maxLevel) {
        List<UserAccessLevel> result = new ArrayList<>();
        try {
            List<UserAccessLevel> levels = sessionManagement.getDictionaryDAO(UserAccessLevelDAOImpl.class, USER_ACCESS_LEVEL_DAO).findDocuments();
            for (UserAccessLevel level : levels) {
                if (level.getLevel() <= maxLevel) {
                    result.add(level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<UserAccessLevel> getUserAccessLevelsGreaterOrEqualMaxValue(int maxLevel) {
        List<UserAccessLevel> result = new ArrayList<>();
        try {
            List<UserAccessLevel> levels = sessionManagement.getDictionaryDAO(UserAccessLevelDAOImpl.class, USER_ACCESS_LEVEL_DAO).findDocuments();
            for (UserAccessLevel level : levels) {
                if (level.getLevel() >= maxLevel) {
                    result.add(level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<UserAccessLevel> getUserAccessLevelsWithEmptyValue() {
        List<UserAccessLevel> result = new ArrayList<>();
        try {
            result = sessionManagement.getDictionaryDAO(UserAccessLevelDAOImpl.class, USER_ACCESS_LEVEL_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserAccessLevel empty = new UserAccessLevel();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }


    public List<SenderType> getSenderTypes() {
      return sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, SENDER_TYPE_DAO).getItems();
    }


    public List<DeliveryType> getDeliveryTypes() {
       return sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, RB_DELIVERY_TYPE_DAO).getItems();
    }



    public List<GroupType> getGroupTypes() {
        List<GroupType> result = new ArrayList<>();
        try {
            result = sessionManagement.getDictionaryDAO(GroupTypeDAOImpl.class, GROUP_TYPE_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<DocumentForm> getDocumentForms() {
        return sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findDocuments();
    }

    public List<DocumentForm> getDocumentFormsWithEmptyValue() {
        List<DocumentForm> result = getDocumentForms();
        DocumentForm empty = new DocumentForm();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }

    public List<DocumentForm> getDocumentFormsByCategory(String category) {
        return sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByDocumentTypeCode(category);
    }

    public List<DocumentForm> getDocumentFormsByCategoryWithEmptyValue(String category) {
        List<DocumentForm> result = getDocumentFormsByCategory(category);
        DocumentForm empty = new DocumentForm();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }

    public List<Nomenclature> getNomenclature() {
        return sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).getItems();
    }

    public List<DocumentType> getDocumentTypes(){
        return sessionManagement.getDictionaryDAO(DocumentTypeDAOImpl.class, RB_DOCUMENT_TYPE_DAO).getItems();

    }

    public Nomenclature getNomenclatureByUser(User user) {
        return sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).getUserDefaultNomenclature(user);
    }

    public List<ContactInfoType> getContactTypes() {
        return sessionManagement.getDictionaryDAO(ContactInfoTypeDAOImpl.class, RB_CONTACT_TYPE_DAO).getItems();
    }

    public List<ContragentType> getContragentTypes() {
        return sessionManagement.getDictionaryDAO(ContragentTypeDAOImpl.class, RB_CONTRAGENT_TYPE_DAO).getItems();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}