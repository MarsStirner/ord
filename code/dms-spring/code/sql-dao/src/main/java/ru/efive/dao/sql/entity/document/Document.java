package ru.efive.sql.entity.document;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.user.User;

//import javax.faces.context.FacesContext;
import javax.persistence.*;

/*import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;*/

import java.util.Date;

/**
 * Базовый класс - документ
 */
@MappedSuperclass
public class Document extends IdentifiedEntity {

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public User getEditor() {
		return editor;
	}

	public void setEditor(User editor) {
		this.editor = editor;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	/*@PrePersist
	protected void onCreate() {
		created = new Date();
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null && context.getApplication() != null) {
			Object sessionManagement = context.getELContext().getELResolver().getValue(context.getELContext(), null, "#{sessionManagement}");
			if (sessionManagement != null) {
				try {
					Object loggedUser = PropertyUtils.getProperty(sessionManagement, "loggedUser");
					if (loggedUser != null) {
						setAuthor((User) loggedUser);
					}
				}
				catch (Exception e) {
					Logger.getRootLogger().error("Unable to find property", e);
				}
			}
		}
	}
	
	@PreUpdate
	protected void onUpdate() {
		modified = new Date();
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null && context.getApplication() != null) {
			Object sessionManagement = context.getELContext().getELResolver().getValue(context.getELContext(), null, "#{sessionManagement}");
			if (sessionManagement != null) {
				try {
					Object loggedUser = PropertyUtils.getProperty(sessionManagement, "loggedUser");
					if (loggedUser != null) {
						setEditor((User) loggedUser);
					}
				}
				catch (Exception e) {
					Logger.getRootLogger().error("Unable to find property", e);
				}
			}
		}
	}*/
	
	
	/**
	 * Дата создания
	 */
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date created;

	/**
	 * Дата последнего редактирования
	 */
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date modified;

	/**
	 * Автор
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	private User author;
	
	/**
	 * Последний редактор
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	private User editor;
	
	/**
	 * Удален ли документ
	 */
	private boolean deleted;
	
	
	private static final long serialVersionUID = -5542939516927639639L;
}