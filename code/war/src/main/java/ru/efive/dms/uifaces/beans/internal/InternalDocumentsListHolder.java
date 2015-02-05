package ru.efive.dms.uifaces.beans.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.ViewFactDaoImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.user.User;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.efive.dms.util.ApplicationDAONames.INTERNAL_DOCUMENT_FORM_DAO;
import static ru.efive.dms.util.ApplicationDAONames.VIEW_FACT_DAO;

@ManagedBean(name = "internal_documents")
@ViewScoped
public class InternalDocumentsListHolder extends AbstractDocumentListHolderBean<InternalDocument> {

    private static final Logger logger = LoggerFactory.getLogger("INTERNAL_DOCUMENT");

    @PostConstruct
    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    public void initInternalDocumentList(){
        if(!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                logger.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    logger.info("{} = {}", entry.getKey(), entry.getValue());
                    filters.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", false);
        //Сортировка по-умолчанию (дата регистрации документа)
    }

    @Override
    protected int getTotalCount() {
        if (!sessionManagement.isSubstitution()) {
            //Без замещения
            return new Long(
                    sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                            .countAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false)
            ).intValue();
        }  else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return new Long(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).countAllDocumentsByUserList(filters, filter, userList, false, false)).intValue();
        }
    }

    @Override
    protected List<InternalDocument> loadDocuments() {
        final List<InternalDocument> resultList;
        if (!sessionManagement.isSubstitution()) {
            // Без замещения
           resultList = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            resultList = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUserList(filters, filter, userList, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        }
        //Проверка и вытсавленние классов просмотра документов пользователем
        if (!resultList.isEmpty()) {
            sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).applyViewFlagsOnInternalDocumentList
                    (resultList, sessionManagement.getLoggedUser());
        }
        return resultList;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private Map<String, Object> filters = new HashMap<String, Object>();
    //Initialize block
    {
        filters.put("registrationNumber", "%");
    }

    private String filter;
    private static final long serialVersionUID = 8535420074467871583L;
}