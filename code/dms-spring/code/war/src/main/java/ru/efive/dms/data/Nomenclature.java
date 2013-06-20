package ru.efive.dms.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.efive.sql.entity.DictionaryEntity;

/**
 * Номенклатура документа
 * 
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "persons_unid_by_nomenclature")
public class Nomenclature extends DictionaryEntity {
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	
	/**
	 * Категория номенклатуры
	 */
	private String category;

	/**
	 * Описание номенклатуры
	 */
	private String description;
	
	private static final long serialVersionUID = 7864611988027689367L;
}
