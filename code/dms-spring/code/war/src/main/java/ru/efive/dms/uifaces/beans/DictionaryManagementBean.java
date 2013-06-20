package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.crm.data.ContragentNomenclature;
import ru.efive.sql.dao.user.GroupTypeDAO;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.efive.sql.entity.enums.GroupType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.dao.DeliveryTypeDAOImpl;
import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.dao.NomenclatureDAOImpl;
import ru.efive.dms.dao.RegionDAOImpl;
import ru.efive.dms.dao.SenderTypeDAOImpl;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.Nomenclature;
import ru.efive.dms.data.Region;
import ru.efive.dms.data.SenderType;
import ru.efive.dms.util.ApplicationHelper;

@Named("dictionaryManagement")
@ConversationScoped
public class DictionaryManagementBean implements Serializable {

	public List<UserAccessLevel> getUserAccessLevels() {
		List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
		try {
			result = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}	

	public List<UserAccessLevel> getUserAccessLevelsLowerOrEqualMaxValue(int maxLevel) {
		List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
		try {
			List<UserAccessLevel> levels=sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
			for(UserAccessLevel level:levels){
				if(level.getLevel()<=maxLevel){result.add(level);}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}	

	public List<UserAccessLevel> getUserAccessLevelsGreaterOrEqualMaxValue(int maxLevel) {
		List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
		try {
			List<UserAccessLevel> levels=sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
			for(UserAccessLevel level:levels){
				if(level.getLevel()>=maxLevel){result.add(level);}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}	
	public List<UserAccessLevel> getUserAccessLevelsWithEmptyValue() {
		List<UserAccessLevel> result = new ArrayList<UserAccessLevel>();
		try {
			result = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		UserAccessLevel empty=new UserAccessLevel();		
		empty.setValue("");
		result.add(0,empty);
		return result;
	}	


	public List<Region> getRegions() {
		List<Region> result = new ArrayList<Region>();
		try {
			result = sessionManagement.getDictionaryDAO(RegionDAOImpl.class, ApplicationHelper.REGION_DAO).findDocuments(this.getFilter(),false);

			Collections.sort(result, new Comparator<Region>() {
				public int compare(Region o1, Region o2) {				
					return o1.getValue().compareTo(o2.getValue());
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<SenderType> getSenderTypes() {
		List<SenderType> result = new ArrayList<SenderType>();
		try {
			result = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, ApplicationHelper.SENDER_TYPE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<SenderType> getSenderTypesWithEmptyValue() {
		List<SenderType> result = new ArrayList<SenderType>();
		try {
			result = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, ApplicationHelper.SENDER_TYPE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		SenderType empty=new SenderType();		
		empty.setValue("");
		result.add(0,empty);
		return result;
	}

	public List<DeliveryType> getDeliveryTypes() {
		List<DeliveryType> result = new ArrayList<DeliveryType>();
		try {
			result = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, ApplicationHelper.DELIVERY_TYPE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	

	public List<DeliveryType> getDeliveryTypesWithEmptyValue() {
		List<DeliveryType> result = new ArrayList<DeliveryType>();
		try {
			result = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, ApplicationHelper.DELIVERY_TYPE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		DeliveryType empty=new DeliveryType();		
		empty.setValue("");
		result.add(0,empty);
		return result;
	}

	public List<GroupType> getGroupTypes() {
		List<GroupType> result = new ArrayList<GroupType>();
		try {
			result = sessionManagement.getDictionaryDAO(GroupTypeDAO.class, ApplicationHelper.GROUP_TYPE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	

	public List<DocumentForm> getDocumentForms() {
		List<DocumentForm> result = new ArrayList<DocumentForm>();
		try {
			result = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}	

	public List<DocumentForm> getDocumentFormsWithEmptyValue() {
		List<DocumentForm> result = new ArrayList<DocumentForm>();
		try {
			result = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		DocumentForm empty=new DocumentForm();		
		empty.setValue("");
		result.add(0,empty);
		return result;
	}	

	public List<DocumentForm> getDocumentFormsByCategory(String category) {		
		List<DocumentForm> result = new ArrayList<DocumentForm>();
		try {
			result = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory(category);		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	

	public List<DocumentForm> getDocumentFormsByCategoryWithEmptyValue(String category) {		
		List<DocumentForm> result = new ArrayList<DocumentForm>();
		try {
			result = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory(category);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		DocumentForm empty=new DocumentForm();		
		empty.setValue("");
		result.add(0,empty);
		return result;
	}	

	public List<DocumentForm> getAllUserDocumentFormsByCategory(User user, String category) {		
		List<DocumentForm> result = new ArrayList<DocumentForm>();
		try {
			Map<String,Object> 	filters=new HashMap<String,Object>();		
			filters.put("category",category);
			result = new ArrayList<DocumentForm>(new HashSet<DocumentForm>(sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, user)));		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	

	public List<Nomenclature> getNomenclature() {
		List<Nomenclature> result = new ArrayList<Nomenclature>();
		try {
			result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, ApplicationHelper.NOMENCLATURE_DAO).findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*public List<IncomingDocument> getRegistratedIncomingDocuments() {
		List<IncomingDocument> result = new ArrayList<IncomingDocument>();
		try {
			result = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, "incomingDAO").findRegistratedDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}*/

	public Nomenclature getNomenclatureByUserLogin(String login) {
		List<Nomenclature> result = new ArrayList<Nomenclature>();
		try {
			result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, ApplicationHelper.NOMENCLATURE_DAO).findByDescription(login);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result.get(0);
	}

	public Nomenclature getNomenclatureByUserUNID(String unid) {
		List<Nomenclature> result = new ArrayList<Nomenclature>();
		try {
			result = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, ApplicationHelper.NOMENCLATURE_DAO).findByDescription(unid);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return (result==null||result.size()==0)?null:result.get(0);
	}


	public List<ContragentNomenclature> getContragentNomenclature() {
		List<ContragentNomenclature> result = new ArrayList<ContragentNomenclature>();
		try {
			result = sessionManagement.getDictionaryDAO(ru.efive.crm.dao.NomenclatureDAOImpl.class, "contragentNomenclatureDao").findDocuments();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}

	private String filter;

	@Inject @Named("sessionManagement")
	SessionManagementBean sessionManagement = new SessionManagementBean();
}