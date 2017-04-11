package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.ReferenceBookEntity;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Created by EUpatov on 11.04.2017.
 */
@Transactional("ordTransactionManager")
public abstract class AbstractReferenceBookConverter<T extends ReferenceBookEntity> implements Converter {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReferenceBookDao<T> dao;

    public AbstractReferenceBookConverter(ReferenceBookDao<T> referenceBookDao) {
        this.dao = referenceBookDao;
    }

    @PostConstruct
    public void lookupDao() {
        log.info("Converter initialized. SELF[{}] DAO[{}] ", this, dao.getClass().getSimpleName());
    }


    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            return dao.getByCode(value);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            return ((ReferenceBookEntity) o).getCode();
        }
        return null;
    }
}
