package ru.efive.dms.uifaces.beans.contragent;

/**
 * Author: Upatov Egor <br>
 * Date: 12.02.2015, 20:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

import static ru.efive.dms.util.message.MessageHolder.*;

@Controller("contragentType")
@SpringScopeView
public class ContragentTypeHolderBean extends AbstractDocumentHolderBean<ContragentType> {


    @Autowired
    @Qualifier("contragentDao")
    private ContragentDao contragentDao;
    @Autowired
    @Qualifier("contragentTypeDao")
    private ContragentTypeDao contragentTypeDao;

    @Override
    protected boolean deleteDocument() {
        final ContragentType document = getDocument();
        final List<Contragent> contragentsWithThisContragentType = contragentDao.getByType(document);
        if (contragentsWithThisContragentType.isEmpty()) {
            document.setDeleted(true);
            final ContragentType afterDelete = contragentTypeDao.save(document);
            if (afterDelete != null) {
                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
                } catch (IOException e) {
                    log.error("Error in redirect ", e);
                }
                return true;
            } else {
                MessageUtils.addMessage(MSG_CANT_DELETE);
                return false;
            }
        } else {
            MessageUtils.addMessage(MSG_RB_CONTRAGENT_TYPE_IS_USED_BY_SOME_CONTRAGENTS);
            return false;
        }
    }

    @Override
    protected void initNewDocument() {
        final ContragentType document = new ContragentType();
        document.setDeleted(false);
        setDocument(document);
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            final ContragentType document = contragentTypeDao.get(id);
            if (document == null) {
                setDocumentNotFound();
            } else {
                setDocument(document);
            }
        } catch (Exception e) {
            MessageUtils.addMessage(MSG_ERROR_ON_INITIALIZE);
            log.error("initializeError", e);
        }
    }

    @Override
    public boolean saveNewDocument() {
        try {
            final ContragentType document = contragentTypeDao.save(getDocument());
            if (document == null) {
                MessageUtils.addMessage(MSG_CANT_SAVE);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error on save New", e);
            MessageUtils.addMessage(MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            ContragentType document = contragentTypeDao.update(getDocument());
            if (document == null) {
                MessageUtils.addMessage(MSG_CANT_SAVE);
                return false;
            }
            setDocument(document);
            return true;
        } catch (Exception e) {
            log.error("Error on save", e);
            MessageUtils.addMessage(MSG_ERROR_ON_SAVE);
            return false;
        }
    }
}
