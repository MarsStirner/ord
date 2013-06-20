package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("outDocumentsByNumber")
@SessionScoped
public class OutgoingDocumentsByNumberHolder extends AbstractDocumentListHolderBean<OutgoingDocument> {
	private static final String beanName= "outDocumentsByNumber";
	
	private List<OutgoingDocument> hashDocuments;	
	private boolean needRefresh=true; 

	public boolean isNeedRefresh() {
		return needRefresh;
	}

	public void setNeedRefresh(boolean needRefresh) {	
		if(needRefresh==true){
			this.markNeedRefresh();
		}
		this.needRefresh = needRefresh;
	}

	protected List<OutgoingDocument> getHashDocuments(int fromIndex, int toIndex){		
		List<OutgoingDocument> result=new ArrayList<OutgoingDocument>();
		if(needRefresh){	
			sessionManagement.registrateBeanName(beanName);
			try {			
				User user=sessionManagement.getLoggedUser(); 
				user=sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());				
				this.hashDocuments=new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user,false, false)));
				
				Collections.sort(this.hashDocuments, new Comparator<OutgoingDocument>() {
					public int compare(OutgoingDocument o1, OutgoingDocument o2) {								
						Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
						c1.setTime(o1.getCreationDate());
						Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
						c2.setTime(o2.getCreationDate());
						return c2.compareTo(c1);						
					}
				});													
				needRefresh=false;					
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
		toIndex=(this.hashDocuments.size()<fromIndex+toIndex)?this.hashDocuments.size():fromIndex+toIndex;
		result=this.hashDocuments.subList(fromIndex, toIndex);			
		return result;
	}

	protected List<OutgoingDocument> getHashDocuments(){		
		List<OutgoingDocument> result=new ArrayList<OutgoingDocument>();
		if(needRefresh){
			sessionManagement.registrateBeanName(beanName);
			try {			
				User user=sessionManagement.getLoggedUser(); 
				user=sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());				
				result=new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filters, filter, user,false, false)));
				Collections.sort(result, new Comparator<OutgoingDocument>() {
					public int compare(OutgoingDocument o1, OutgoingDocument o2) {								
						Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
						c1.setTime(o1.getCreationDate());
						Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
						c2.setTime(o2.getCreationDate());
						return c2.compareTo(c1);						
					}
				});		
				
				this.hashDocuments=result;
				needRefresh=false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}else{			
			result=this.hashDocuments;
			//needRefresh=false;
		}
		return result;
	}
	
	@Override
	protected Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}

	@Override
	protected Sorting initSorting() {		
		return new Sorting("registrationDate", false);
	}

	/*@Override
	protected int getTotalCount() {
		int result = 0;
		try {
			User user=sessionManagement.getLoggedUser(); 
			user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
			return new Long(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).countAllDocumentsByUser(filter,
					user, false, false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected List<OutgoingDocument> loadDocuments() {		
		List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
		try {			
			User user=sessionManagement.getLoggedUser(); 
			user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
			result = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByUser(filter, user,
					false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
			result = new ArrayList<OutgoingDocument>(new HashSet<OutgoingDocument>(result));
			Collections.sort(result, new Comparator<OutgoingDocument>() {
				public int compare(OutgoingDocument o1, OutgoingDocument o2) {								
					Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
					c1.setTime(o1.getRegistrationDate());
					Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
					c2.setTime(o2.getRegistrationDate());
					return c2.compareTo(c1);									
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
*/
	
	@Override
	protected int getTotalCount() {		
		int result = 0;
		try {			
			result= new Long(this.getHashDocuments().size()).intValue();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}

	@Override
	protected List<OutgoingDocument> loadDocuments() {
		List<OutgoingDocument> result = new ArrayList<OutgoingDocument>();
		try {
			result=this.getHashDocuments(getPagination().getOffset(), getPagination().getPageSize());			


		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}

	@Override	
	public void refresh() {
		this.needRefresh=true;
		super.refresh();		
	}
	
	@Override
	public List<OutgoingDocument> getDocuments() {
		return super.getDocuments();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.needRefresh=true;
		this.filter = filter;
	}


	private String filter;
	static private Map<String,Object> 	filters=new HashMap<String,Object>();
	static {filters.put("registrationNumber", "%");}
	
	@Inject @Named("sessionManagement")
	private transient SessionManagementBean sessionManagement;

	private static final long serialVersionUID = 8535420074467871583L;
}