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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("tasksList")
@SessionScoped
public class TasksEqTaskHolder extends AbstractDocumentListHolderBean<Task> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6711725735345862233L;

	protected List<Task> getHashDocuments(int fromIndex, int toIndex){		
		List<Task> result=new ArrayList<Task>();
		if(needRefresh){			
			try {			
				User user=sessionManagement.getLoggedUser(); 
				user=sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());				
				this.hashDocuments=new ArrayList<Task>(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findAllDocumentsByUser(filters, filter, user,false, false)));				

				Collections.sort(this.hashDocuments, new Comparator<Task>() {
					public int compare(Task o1, Task o2) {								
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

	protected List<Task> getHashDocuments(){		
		List<Task> result=new ArrayList<Task>();
		if(needRefresh){
			try {			
				User user=sessionManagement.getLoggedUser(); 
				user=sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());				
				result=new ArrayList<Task>(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findAllDocumentsByUser(filters, filter, user,false, false)));				
				Collections.sort(result, new Comparator<Task>() {
					public int compare(Task o1, Task o2) {								
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
		return new Sorting("creationDate", false);
	}

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
	protected List<Task> loadDocuments() {		
		List<Task> result = new ArrayList<Task>();
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
	public List<Task> getDocuments() {
		String key=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("key");		
		if(key!=null && !key.isEmpty()){								
			if(!filters.containsKey(key)){				
					this.needRefresh=true;
					markNeedRefresh();
					this.filters.clear();
					String value=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("value");
					this.filters.put(key, value);	
			}			
		}
		return super.getDocuments();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.needRefresh=true;
		this.filter = filter;
	}

	private List<Task> hashDocuments;	
	private boolean needRefresh=true; 
	
	private String filter;	
	static private Map<String,Object> 	filters=new HashMap<String,Object>();
	static {
		filters.put("formDescription", "task");
		filters.put("formCategory", "Поручения");
	}

	@Inject @Named("sessionManagement")
	private transient SessionManagementBean sessionManagement;

}