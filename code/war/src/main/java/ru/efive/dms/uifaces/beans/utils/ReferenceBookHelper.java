package ru.efive.dms.uifaces.beans.utils;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

import java.util.List;

/**
 * Created by EUpatov on 05.04.2017.
 */
@Component("refBookHelper")
@Transactional(value = "ordTransactionManager", propagation = Propagation.REQUIRED)
public class ReferenceBookHelper {
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    @Autowired
    @Qualifier("deliveryTypeDao")
    private DeliveryTypeDao deliveryTypeDao;

    @Autowired
    @Qualifier("userAccessLevelDao")
    private UserAccessLevelDao userAccessLevelDao;

    public List<DocumentForm> getDocumentFormsByCategory(String category) {
        return documentFormDao.findByDocumentTypeCode(category);
    }

    public List<DeliveryType> getDeliveryTypes() {
        return deliveryTypeDao.getItems();
    }

    public List<UserAccessLevel> getUserAccessLevelsGreaterOrEqualMaxValue(int level) {
        return userAccessLevelDao.findLowerThenLevel(level);
    }
}
