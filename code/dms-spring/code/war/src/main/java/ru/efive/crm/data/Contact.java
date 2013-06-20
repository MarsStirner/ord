package ru.efive.crm.data;

import javax.persistence.*;

import ru.efive.sql.entity.IdentifiedEntity;

/**
 * Контакты
 * 
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "contacts")
public class Contact extends IdentifiedEntity {
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * полное имя
	 */
	@Transient
	public String getDescription() {
		return lastName + " " + (firstName != null && !firstName.equals("")? firstName + " ": "") + 
		(middleName != null && !middleName.equals("")? middleName: "");
	}
	
	/**
	 * краткая форма полного имени
	 */
	@Transient
	public String getDescriptionShort() {
		return lastName + " " + (firstName != null && !firstName.equals("")? firstName.substring(0, 1) + ". ": "") + 
		(middleName != null && !middleName.equals("")? middleName.substring(0, 1) + ".": "");
	}

	public Contragent getContragent() {
		return contragent;
	}

	public void setContragent(Contragent contragent) {
		this.contragent = contragent;
	}
	
	/**
	 * фамилия
	 */
	private String lastName;

	/**
	 * отчество
	 */
	private String middleName;

	/**
	 * имя
	 */
	private String firstName;
	
	/**
	 * Организация
	 */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private Contragent contragent;
	
	private static final long serialVersionUID = 2676252421021362937L;
}
