package ru.efive.dms.uifaces.beans.utils;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

import java.util.List;

/**
 * Created by EUpatov on 05.04.2017.
 */
@Component("refBookHelper")
public class ReferenceBookHelper {
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    public List<DocumentForm> getDocumentFormsByCategory(String category) {
        return documentFormDao.findByDocumentTypeCode(category);
    }
}
