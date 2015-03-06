package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 05.03.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: Абстрактный класс для поисковых бинов <br>
 */
public abstract class AbstractDocumentSearchBean<T> {

    /**
     * Набор фильтров в виде "Ключ"->"Значения"
     */
    protected Map<String, Object> filters = new HashMap<String, Object>();

    /**
     * Список отобранных документов
     */
    protected List<T> searchResults = new ArrayList<T>(0);

    public List<T> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<T> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * Очистка текущего фильтра
     */
    public void clearFilter(){
        filters.clear();
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     * @param KEY  ключ
     * @param value значение (если NULL -> добавления не произойдет)
     */
    protected void putNotNullToFilters(String KEY, Object value) {
        if(value != null){
            filters.put(KEY, value);
        }
    }

    /**
     * Добавить в фильтры ненелувое значение с ключом
     * @param KEY  ключ
     * @param value значение (если NULL -> добавления не произойдет)
     */
    protected void putNotNullToFilters(String KEY, String value) {
        if (StringUtils.isNotEmpty(value)) {
            filters.put(KEY, value);
        }
    }



    /**
     * Выполнить поиск с текущим фильтром
     * @return  Список документов, удовлетворяющих поиску
     */
    public abstract List<T> performSearch();




}
