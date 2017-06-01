package ru.efive.dms.uifaces.beans;


import org.primefaces.model.menu.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractLoggableBean;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentTypeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;


@Component("menuView")
@Scope("session")
@Transactional
public class MenuView extends AbstractLoggableBean{

    private MenuModel root;

    @Autowired
    public MenuView(
            final PlatformTransactionManager transactionManager,
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("documentTypeDao") final DocumentTypeDao documentTypeDao,
            @Qualifier("documentFormDao") final DocumentFormDao documentFormDao
    ) {
        log.info("{} start initialization", getBeanName());
        root = new DefaultMenuModel();
        //TODO сделать общее решение для типов и видов документов и статусов и специфинчных штук (на контроле входящие)
//        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
//        for (DocumentType documentType : documentTypeDao.getItems()) {
//            final DefaultSubMenu subMenu = new DefaultSubMenu(documentType.getValue());
//            final String typeCode = documentType.getCode().toLowerCase();
//            final DefaultMenuItem personal = new DefaultMenuItem("Мои проекты", "leafIcon", "/component/" + typeCode + "/personal.xhtml");
//            subMenu.addElement(personal);
//            final DefaultMenuItem list = new DefaultMenuItem("По номеру", "leafIcon", "/component/" + typeCode + "/list.xhtml");
//            subMenu.addElement(list);
//            for (DocumentForm form : documentFormDao.findByDocumentType(documentType)) {
//                if (form.isVisibleInMenu()) {
//                    final DefaultMenuItem item = new DefaultMenuItem(form.getValue(),"leafIcon","/component/" + typeCode + "/list.xhtml" );
//                    item.setParam("formCode", form.getCode());
//                    subMenu.addElement(item);
//                }
//            }
//            root.addElement(subMenu);
//        }
//        transactionManager.commit(transaction);
//        if (!authData.isOuter()) {
//            final DefaultSubMenu subMenu = new DefaultSubMenu("Пользователи");
//            root.addElement(subMenu);
//            final DefaultMenuItem users = new DefaultMenuItem("Телефонный справочник", "leafIcon", "/component/users.xhtml");
//            subMenu.addElement(users);
//            final DefaultMenuItem groups = new DefaultMenuItem("Группы", "leafIcon", "/component/groups.xhtml");
//            subMenu.addElement(groups);
//            if (authData.isFilling()) {
//                final DefaultMenuItem substitutions = new DefaultMenuItem("Замещение", "leafIcon", "/component/user_substitutions.xhtml");
//                subMenu.addElement(substitutions);
//            }
//        }
//        if (authData.isAdministrator() || authData.isOfficeManager() || authData.isRequestManager()) {
//            final DefaultSubMenu subMenu = new DefaultSubMenu("Контрагенты");
//            root.addElement(subMenu);
//            final DefaultMenuItem contragents = new DefaultMenuItem("По названию", "leafIcon", "/component/contragents.xhtml");
//            subMenu.addElement(contragents);
//            final DefaultMenuItem contragentTypes = new DefaultMenuItem("Типы контрагентов", "leafIcon", "/component/contragentTypes.xhtml");
//            subMenu.addElement(contragentTypes);
//        }
//        if (authData.isAdministrator() || authData.isRecorder()) {
//            final DefaultSubMenu subMenu = new DefaultSubMenu("Архивирование");
//            root.addElement(subMenu);
//            final DefaultMenuItem office_keeping_files = new DefaultMenuItem("Дела", "leafIcon", "/component/office_keeping_files.xhtml");
//            subMenu.addElement(office_keeping_files);
//            final DefaultMenuItem office_keeping_volumes = new DefaultMenuItem("Тома дел", "leafIcon", "/component/office_keeping_volumes.xhtml");
//            subMenu.addElement(office_keeping_volumes);
//        }
//        if (authData.isAdministrator()) {
//            final DefaultSubMenu subMenu = new DefaultSubMenu("Администрирование");
//            root.addElement(subMenu);
//            final DefaultMenuItem users = new DefaultMenuItem("Телефонный справочник", "leafIcon", "/component/users.xhtml");
//            subMenu.addElement(users);
//            final DefaultMenuItem roles = new DefaultMenuItem("Роли", "leafIcon", "/component/roles.xhtml");
//            subMenu.addElement(roles);
//            final DefaultMenuItem numerators = new DefaultMenuItem("Нумераторы", "leafIcon", "/component/numerators.xhtml");
//            subMenu.addElement(numerators);
//            final DefaultMenuItem editableMatrix = new DefaultMenuItem("Матрица редактируемости", "leafIcon", "/component/admin/editableMatrix.xhtml");
//            subMenu.addElement(editableMatrix);
//            final DefaultMenuItem about = new DefaultMenuItem("О программе", "leafIcon");
//            about.setParam("a", "a");
//            about.setParam("b", 1);
//            about.setCommand("#{menuItem.call}");
//            subMenu.addElement(about);
//        }
//        if (authData.isAdministrator() || authData.isOfficeManager() || authData.isRequestManager()) {
//            final DefaultMenuItem reports = new DefaultMenuItem("Отчеты", "leafIcon", "/component/report/list.xhtml");
//            root.addElement(reports);
//        }
        log.info("{} initialized!", getBeanName());
    }

    public MenuModel getRoot() {
        return root;
    }


    public void call(){

    }
}
