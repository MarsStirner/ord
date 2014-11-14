package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.*;
import ru.efive.sql.dao.user.GroupTypeDAO;
import ru.efive.sql.dao.user.RbContactTypeDAO;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.entity.model.crm.ContragentNomenclature;
import ru.entity.model.document.*;
import ru.entity.model.enums.GroupType;
import ru.entity.model.user.RbContactInfoType;
import ru.entity.model.user.UserAccessLevel;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.*;


@Named("dictionaryManagement")
@ConversationScoped
public class DictionaryManagementBean implements Serializable {

    public List<UserAccessLevel> getUserAccessLevels() {
        return sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, USER_ACCESS_LEVEL_DAO).findDocuments();
    }

    public List<UserAccessLevel> getUserAccessLevelsLowerOrEqualMaxValue(int maxLevel) {
        List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
        try {
            List<UserAccessLevel> levels = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, USER_ACCESS_LEVEL_DAO).findDocuments();
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
        List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
        try {
            List<UserAccessLevel> levels = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, USER_ACCESS_LEVEL_DAO).findDocuments();
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
        List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
        try {
            result = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, USER_ACCESS_LEVEL_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserAccessLevel empty = new UserAccessLevel();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }


    public List<Region> getRegions() {
        List<Region> result = new ArrayList<Region>();
        try {
            result = sessionManagement.getDictionaryDAO(RegionDAOImpl.class, REGION_DAO).findDocuments(this.getFilter(), false);

            Collections.sort(result, new Comparator<Region>() {
                public int compare(Region o1, Region o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<SenderType> getSenderTypes() {
        List<SenderType> result = new ArrayList<SenderType>();
        try {
            result = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, SENDER_TYPE_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<SenderType> getSenderTypesWithEmptyValue() {
        List<SenderType> result = getSenderTypes();
        SenderType empty = new SenderType();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }

    public List<DeliveryType> getDeliveryTypes() {
        List<DeliveryType> result = new ArrayList<DeliveryType>();
        try {
            result = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, DELIVERY_TYPE_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<DeliveryType> getDeliveryTypesWithEmptyValue() {
        List<DeliveryType> result = getDeliveryTypes();
        DeliveryType empty = new DeliveryType();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }

    public List<GroupType> getGroupTypes() {
        List<GroupType> result = new ArrayList<GroupType>();
        try {
            result = sessionManagement.getDictionaryDAO(GroupTypeDAO.class, GROUP_TYPE_DAO).findDocuments();
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
        return sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategory(category);
    }

    public List<DocumentForm> getDocumentFormsByCategoryWithEmptyValue(String category) {
        List<DocumentForm> result = getDocumentFormsByCategory(category);
        DocumentForm empty = new DocumentForm();
        empty.setValue("");
        result.add(0, empty);
        return result;
    }

    public List<Nomenclature> getNomenclature() {
        List<Nomenclature> result = new ArrayList<Nomenclature>();
        try {
            result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, NOMENCLATURE_DAO).findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Nomenclature getNomenclatureByUserLogin(String login) {
        List<Nomenclature> result = new ArrayList<Nomenclature>();
        try {
            result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, NOMENCLATURE_DAO).findByDescription(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.get(0);
    }

    public Nomenclature getNomenclatureByUserUNID(String unid) {
        List<Nomenclature> result = new ArrayList<Nomenclature>();
        try {
            result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, NOMENCLATURE_DAO).findByDescription(unid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (result == null || result.isEmpty()) ? null : result.get(0);
    }


    public List<ContragentNomenclature> getContragentNomenclature() {
        List<ContragentNomenclature> result = new ArrayList<ContragentNomenclature>();
        try {
            result = sessionManagement.getDictionaryDAO(ru.efive.crm.dao.NomenclatureDAOImpl.class, "contragentNomenclatureDao").findDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<RbContactInfoType> getContactTypes() {
        return sessionManagement.getDictionaryDAO(RbContactTypeDAO.class, RB_CONTACT_TYPE_DAO).findDocuments();
    }


    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
}