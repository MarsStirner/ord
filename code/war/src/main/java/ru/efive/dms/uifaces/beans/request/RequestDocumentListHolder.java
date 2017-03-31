package ru.efive.dms.uifaces.beans.request;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForRequestDocument;
import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.*;

@Controller("request_documents")
@SpringScopeView
public class RequestDocumentListHolder extends AbstractDocumentLazyDataModelBean<RequestDocument> {

    private static final Logger logger = LoggerFactory.getLogger("REQUEST_DOCUMENT");
    private Map<String, Object> filters = new HashMap<>();

    @Autowired
    @Qualifier("requestDocumentLDM")
    private LazyDataModelForRequestDocument requestDocumentLDM;

    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    @PostConstruct
    public void initInternalDocumentList() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                logger.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    final String value = entry.getValue();
                    logger.info("{} = {}", entry.getKey(), value);
                    if (value.startsWith("{") && value.endsWith("}")) {
                        // Список
                        final List<String> strings = Arrays.asList(value.substring(1, value.length() - 1).split("\\s*,\\s*"));
                        if (!strings.isEmpty()) {
                            //Для некоторых парметров надо приводить типы
                            if (DocumentSearchMapKeys.STATUS_LIST_KEY.equals(entry.getKey())) {
                                final Set<Integer> ints = new HashSet<>(strings.size());
                                for (String string : strings) {
                                    ints.add(Integer.valueOf(string));
                                }
                                filters.put(entry.getKey(), ints);
                            } else {
                                filters.put(entry.getKey(), new ArrayList<>(strings));
                            }
                        }
                    } else {
                        //Одиночное значение
                        filters.put(entry.getKey(), value);
                    }
                }
            }

        }
        requestDocumentLDM.setFilters(filters);
        setLazyModel(requestDocumentLDM);
    }
}