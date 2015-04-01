package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.commons.lang.StringUtils;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;

/**
 * Author: Upatov Egor <br>
 * Date: 05.03.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: Абстрактный класс для поисковых бинов <br>
 */
public abstract class AbstractDocumentSearchBean<T extends IdentifiedEntity> extends AbstractDocumentLazyDataModelBean<T> {

    /**
     * Набор фильтров в виде "Ключ"->"Значения"
     */
    protected Map<String, Object> filters = new HashMap<String, Object>();


    /**
     * Очистка текущего фильтра
     */
    public void clearFilter(){
        filters.clear();
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     * @param KEY  ключ
     * @param value значение (если NULL -> добавления не произойдет, а вот старое значение удалим)
     */
    protected void putNotNullToFilters(String KEY, Object value) {
        if(value != null){
            filters.put(KEY, value);
        }  else {
            filters.remove(KEY);
        }
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     * @param KEY  ключ
     * @param value значение (если NULL -> добавления не произойдет, а вот старое значение удалим)
     */
    protected void putNotNullToFilters(String KEY, String value) {
        if (StringUtils.isNotEmpty(value)) {
            filters.put(KEY, value);
        }  else {
            filters.remove(KEY);
        }
    }



    /**
     * Выполнить поиск с текущим фильтром
     * @return  Список документов, удовлетворяющих поиску
     */
    public abstract void performSearch();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Параметры поиска *** ОБЩИЕ для всех типов документов *****  /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Статус
    public void setStatus(final String value) {
        putNotNullToFilters(STATUS_KEY, value);
    }

    public String getStatus() {
        return (String) filters.get(STATUS_KEY);
    }

    //  Вид документа
    public void setForm(final DocumentForm value) {
        putNotNullToFilters(FORM_KEY, value);
    }

    public DocumentForm getForm() {
        return (DocumentForm) filters.get(FORM_KEY);
    }

    // Автор
    public void setAuthors(final List<User> value) {
        putNotNullToFilters(AUTHORS_KEY, value);
    }

    public List<User> getAuthors() {
        return (List<User>) filters.get(AUTHORS_KEY);
    }

    public void removeAuthor(User author) {
        final List<User> authors = getAuthors();
        authors.remove(author);
        if(authors.isEmpty()){
            filters.remove(AUTHORS_KEY);
        }
    }

    // Регистрационный номер
    public void setRegistrationNumber(final String value) {
        putNotNullToFilters(REGISTRATION_NUMBER_KEY, value);
    }

    public String getRegistrationNumber() {
        return (String) filters.get(REGISTRATION_NUMBER_KEY);
    }

    // Дата создания ОТ
    public void setStartCreationDate(Date value) {
        putNotNullToFilters(START_CREATION_DATE_KEY, value);
    }

    public Date getStartCreationDate() {
        return (Date) filters.get(START_CREATION_DATE_KEY);
    }

    // Дата создания ДО
    public void setEndCreationDate(Date value) {
        putNotNullToFilters(END_CREATION_DATE_KEY, value);
    }

    public Date getEndCreationDate() {
        return (Date) filters.get(END_CREATION_DATE_KEY);
    }

    // Дата регистрации ОТ
    public void setStartRegistrationDate(Date value) {
        putNotNullToFilters(START_REGISTRATION_DATE_KEY, value);
    }

    public Date getStartRegistrationDate() {
        return (Date) filters.get(START_REGISTRATION_DATE_KEY);
    }

    // Дата регистрации ДО
    public void setEndRegistrationDate(Date value) {
        putNotNullToFilters(END_REGISTRATION_DATE_KEY, value);
    }

    public Date getEndRegistrationDate() {
        return (Date) filters.get(END_REGISTRATION_DATE_KEY);
    }

    // Краткое содержание
    public void setShortDescription(final String value) {
        putNotNullToFilters(SHORT_DESCRIPTION_KEY, value);
    }

    public String getShortDescription() {
        return (String) filters.get(SHORT_DESCRIPTION_KEY);
    }

}
