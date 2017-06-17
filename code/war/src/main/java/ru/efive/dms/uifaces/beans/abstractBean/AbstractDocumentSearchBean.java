package ru.efive.dms.uifaces.beans.abstractBean;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.User;
import ru.entity.model.workflow.Status;

import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.*;

/**
 * Author: Upatov Egor <br>
 * Date: 05.03.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: Абстрактный класс для поисковых бинов <br>
 */
public abstract class AbstractDocumentSearchBean<T extends DeletableEntity> extends AbstractDocumentLazyDataModelBean<T> {

    /**
     * Набор фильтров в виде "Ключ"->"Значения"
     */
    protected Map<String, Object> filters = new HashMap<>();

    protected boolean searchPerformed = false;


    /**
     * Очистка текущего фильтра
     */
    public void clearFilter() {
        filters.clear();
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     *
     * @param KEY   ключ
     * @param value значение (если NULL -> добавления не произойдет, а вот старое значение удалим)
     */
    protected void putNotNullToFilters(String KEY, Object value) {
        if (value != null) {
            filters.put(KEY, value);
        } else {
            filters.remove(KEY);
        }
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     *
     * @param KEY   ключ
     * @param value значение (если NULL -> добавления не произойдет, а вот старое значение удалим)
     */
    protected void putNotNullToFilters(String KEY, String value) {
        if (StringUtils.isNotEmpty(value)) {
            filters.put(KEY, value);
        } else {
            filters.remove(KEY);
        }
    }

    public boolean isSearchPerformed() {
        return searchPerformed;
    }

    public void setSearchPerformed(final boolean searchPerformed) {
        this.searchPerformed = searchPerformed;
    }


    /**
     * Выполнить поиск с текущим фильтром
     *
     * @return Список документов, удовлетворяющих поиску
     */
    public abstract void performSearch();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Выбора автора ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseAuthors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_AUTHOR));
        final List<User> preselected = getAuthors();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onAuthorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null) {
                setAuthors(selected);
            } else {
                filters.remove(AUTHORS);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска *** ОБЩИЕ для всех типов документов *****  /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Status getStatus() {
        return (Status) filters.get(STATUS);
    }

    // Статус
    public void setStatus(final Status value) {
        putNotNullToFilters(STATUS, value);
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get(FORM);
    }

    //  Вид документа
    public void setForm(final DocumentForm value) {
        putNotNullToFilters(FORM, value);
    }

    public List<User> getAuthors() {
        return (List<User>) filters.get(AUTHORS);
    }

    // Автор
    public void setAuthors(final List<User> value) {
        putNotNullToFilters(AUTHORS, value);
    }

    public void removeAuthor(User author) {
        final List<User> authors = getAuthors();
        authors.remove(author);
        if (authors.isEmpty()) {
            filters.remove(AUTHORS);
        }
    }

    public String getRegistrationNumber() {
        return (String) filters.get(REGISTRATION_NUMBER);
    }

    // Регистрационный номер
    public void setRegistrationNumber(final String value) {
        putNotNullToFilters(REGISTRATION_NUMBER, value);
    }

    public LocalDateTime getStartCreationDate() {
        return (LocalDateTime) filters.get(CREATION_DATE_START);
    }

    // Дата создания ОТ
    public void setStartCreationDate(LocalDateTime value) {
        putNotNullToFilters(CREATION_DATE_START, value);
    }

    public LocalDateTime getEndCreationDate() {
        return (LocalDateTime) filters.get(CREATION_DATE_END);
    }

    // Дата создания ДО
    public void setEndCreationDate(LocalDateTime value) {
        putNotNullToFilters(CREATION_DATE_END, value);
    }

    public LocalDateTime getStartRegistrationDate() {
        return (LocalDateTime) filters.get(REGISTRATION_DATE_START);
    }

    // Дата регистрации ОТ
    public void setStartRegistrationDate(LocalDateTime value) {
        putNotNullToFilters(REGISTRATION_DATE_START, value);
    }

    public LocalDateTime getEndRegistrationDate() {
        return (LocalDateTime) filters.get(REGISTRATION_DATE_END);
    }

    // Дата регистрации ДО
    public void setEndRegistrationDate(LocalDateTime value) {
        putNotNullToFilters(REGISTRATION_DATE_END, value);
    }

    public String getShortDescription() {
        return (String) filters.get(SHORT_DESCRIPTION);
    }

    // Краткое содержание
    public void setShortDescription(final String value) {
        putNotNullToFilters(SHORT_DESCRIPTION, value);
    }

}
