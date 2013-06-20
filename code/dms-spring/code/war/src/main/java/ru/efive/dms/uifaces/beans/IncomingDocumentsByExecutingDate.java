package ru.efive.dms.uifaces.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("in_documents_by_executing_date")
@SessionScoped
public class IncomingDocumentsByExecutingDate extends AbstractDocumentListHolderBean<IncomingDocument> {
	String[] MONTHS=new String[]{"ЯНВАРЬ","ФЕВРАЛЬ","МАРТ","АПРЕЛЬ","МАЙ","ИЮНЬ","ИЮЛЬ","АВГУСТ","СЕНТЯБРЬ","ОКТЯБРЬ","НОЯБРЬ","ДЕКАБРЬ"};

	@Override
	protected Pagination initPagination() {
		return new Pagination(0, getTotalCount(), 100);
	}

	@Override
	protected int getTotalCount() {		
		int result = 0;
		try {
			User user=sessionManagement.getLoggedUser(); 
			user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
			return new Long(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, "incomingDao").countAllDocumentsByUser(filter,  
					user, false, false)).intValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected List<IncomingDocument> loadDocuments() {
		List<IncomingDocument> result = new ArrayList<IncomingDocument>();
		try {
			User user=sessionManagement.getLoggedUser(); 
			user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(),user.getPassword());
			List<IncomingDocument> list = new ArrayList<IncomingDocument>(new HashSet<IncomingDocument>(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, "incomingDao").findControlledDocumentsByUser(filter,user,false)));			 		
			Collections.sort(list, new Comparator<IncomingDocument>() {
				public int compare(IncomingDocument o1, IncomingDocument o2) {
					Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
					c1.setTime(o1.getExecutionDate()!=null?o1.getExecutionDate():o1.getCreationDate());
					Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
					c2.setTime(o2.getExecutionDate()!=null?o2.getExecutionDate():o2.getCreationDate());
					return c1.compareTo(c2);
				}
			});
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");	
			SimpleDateFormat ydf=new SimpleDateFormat("yyyy");
			SimpleDateFormat mdf=new SimpleDateFormat("MM");
			SimpleDateFormat ddf=new SimpleDateFormat("dd");
			Date in_currentDate=sdf.parse(sdf.format(java.util.Calendar.getInstance ().getTime()));
			String category1=null,category2=null,category3 = null;
			for (IncomingDocument document: list) {
				Date in_date=document.getExecutionDate();
				if(in_date!=null&&(in_date.compareTo(in_currentDate)>=0)){					      
					IncomingDocument year=new IncomingDocument();																
					year.setExecutionStringDate(ydf.format(in_date));																
					if((category1==null)||(!ydf.format(in_date).equals(category1))){						
						category1=ydf.format(in_date);
						year.setId(0);
						year.setGrouping(0);
						result.add(year);
						category2=null;
						category3 = null;
					}
					IncomingDocument month=new IncomingDocument();
					if((category2==null)||(!mdf.format(in_date).equals(category2))){						
						category2=mdf.format(in_date);
						month.setId(0);
						month.setGrouping(1);
						month.setExecutionStringDate(MONTHS[Integer.parseInt(mdf.format(in_date))-1]);											
						result.add(month);
						category3 = null;
					}
					IncomingDocument day=new IncomingDocument();
					if((category3==null)||(!ddf.format(in_date).equals(category3))){						
						category3=ddf.format(in_date);
						day.setId(0);
						day.setGrouping(2);
						day.setExecutionStringDate(ddf.format(in_date));											
						result.add(day);
					}
					//document.setId(3);	
					document.setExecutionStringDate(sdf.format(in_date));
					result.add(document);}}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<IncomingDocument> getDocumentsOnExecution(){
		List<IncomingDocument> result = new ArrayList<IncomingDocument>();
		try {
			List<IncomingDocument> list=getDocuments();
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");	
			SimpleDateFormat ydf=new SimpleDateFormat("yyyy");
			SimpleDateFormat mdf=new SimpleDateFormat("MM");
			SimpleDateFormat ddf=new SimpleDateFormat("dd");
			Date in_currentDate=sdf.parse(sdf.format(java.util.Calendar.getInstance ().getTime()));
			String category1=null,category2=null,category3 = null;
			for (IncomingDocument document: list) {
				Date in_date=document.getExecutionDate();
				if(in_date!=null&&(in_date.compareTo(in_currentDate)>=0)){					      
					IncomingDocument year=new IncomingDocument();																
					year.setExecutionStringDate(ydf.format(in_date));																
					if((category1==null)||(!ydf.format(in_date).equals(category1))){						
						category1=ydf.format(in_date);
						year.setId(0);
						year.setGrouping(0);
						result.add(year);
						category2=null;
						category3 = null;
					}
					IncomingDocument month=new IncomingDocument();
					if((category2==null)||(!mdf.format(in_date).equals(category2))){						
						category2=mdf.format(in_date);
						month.setId(0);
						month.setGrouping(1);
						month.setExecutionStringDate(MONTHS[Integer.parseInt(mdf.format(in_date))-1]);											
						result.add(month);
						category3 = null;
					}
					IncomingDocument day=new IncomingDocument();
					if((category3==null)||(!ddf.format(in_date).equals(category3))){						
						category3=ddf.format(in_date);
						day.setId(0);
						day.setGrouping(2);
						day.setExecutionStringDate(ddf.format(in_date));											
						result.add(day);
					}
					//document.setId(3);	
					document.setExecutionStringDate(sdf.format(in_date));
					result.add(document);}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	
	@Override
	public List<IncomingDocument> getDocuments() {
		return super.getDocuments();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	private String filter;

	@Inject @Named("sessionManagement")
	private transient SessionManagementBean sessionManagement;

	private static final long serialVersionUID = 8535420074467871583L;
}