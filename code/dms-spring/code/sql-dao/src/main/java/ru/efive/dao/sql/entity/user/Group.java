package ru.efive.sql.entity.user;

import ru.efive.sql.entity.DictionaryEntity;
import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.GroupType;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Группа пользователей
 */
@Entity
@Table(name = "groups")
public class Group extends DictionaryEntity {

	public List<User> getMembersList() {
		List<User> result = new ArrayList<User>();
		if(members!=null){
			result.addAll(members);
		}
		Collections.sort(result, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}
		});
		return result;		
	}

	public List<User> getMembersListWithEmptyUser() {
		List<User> result = new ArrayList<User>();
		if(members!=null){
			result.addAll(members);
		}
		Collections.sort(result, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}
		});
		User empty= new User();
		empty.setLastName("");
		empty.setFirstName("");
		empty.setMiddleName("");
		result.add(0, empty);
		return result;		
	}

	public Set<User> getMembers() {
		return members;
	}

	public void setMembers(Set<User> members) {
		this.members = members;
	}


	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCategory(GroupType category) {
		this.category = category;
	}

	public GroupType getCategory() {
		return category;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}


	public void setAuthor(User author) {
		this.author = author;
	}

	public User getAuthor() {
		return author;
	}


	/** пользователи */
	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private Set<User> members;

	/**
	 * Категория
	 */
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)	
	@JoinColumn(name= "categoryId")
	private GroupType category;

	/**
	 * Описание
	 */
	private String description;

	/** Алиас */
	private String alias;	
	
	/**
	 * Автор документа
	 */
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	@JoinTable(name= "group_authors")
	private User author;
	
	private static final long serialVersionUID = 6366882739076305392L;
}