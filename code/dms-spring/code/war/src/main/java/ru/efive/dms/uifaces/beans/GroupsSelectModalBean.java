package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.efive.sql.entity.user.Group;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class GroupsSelectModalBean extends ModalWindowHolderBean {

	private static final long serialVersionUID = -1349913466118952127L;

	public GroupsHolderBean getGroupsView() {
		if (groupsView == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			groupsView = (GroupsHolderBean) context.getApplication().evaluateExpressionGet(context, "#{groups}", GroupsHolderBean.class);
		}
		return groupsView;
	}

	public List<Group> getGroups() {
		return groups;
	}
	
	public void setGroups(List<Group> groups) {
		if(groups==null){
			this.groups=new ArrayList<Group>();
		}else{
			this.groups=groups;
		}
	}

	public void select(Group group) {
		groups.add(group);		
	}

	public void unselect(Group group) {
		groups.remove(group);
	}

	public boolean selected(Group group) {
		return groups.contains(group);
	}

	@Override
	protected void doSave() {
		super.doSave();
	}

	@Override
	protected void doShow() {
		super.doShow();
	}

	@Override
	protected void doHide() {
		super.doHide();
	}

	private GroupsHolderBean 	groupsView;
	private List<Group> 		groups = new ArrayList<Group>();	
}