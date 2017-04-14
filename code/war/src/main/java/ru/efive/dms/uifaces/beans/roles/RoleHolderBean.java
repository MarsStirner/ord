package ru.efive.dms.uifaces.beans.roles;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.enums.RoleType;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;

@Controller("role")
@SpringScopeView
public class RoleHolderBean extends AbstractDocumentHolderBean<Role> {

    @Autowired
    @Qualifier("roleDao")
    private RoleDao roleDao;

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            roleDao.delete(getDocument());
            result = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
        }
        return result;
    }

    @Override
    protected void initDocument(Integer id) {
        setDocument(roleDao.get(id));
        if (getDocument() == null) {
            setState(State.ERROR);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
        }
    }

    @Override
    protected void initNewDocument() {
        setDocument(new Role());
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            Role role = roleDao.update(getDocument());
            if (role == null) {
                addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(role);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            addMessage(null, MSG_ERROR_ON_SAVE);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean saveNewDocument() {
        boolean result = false;
        try {
            Role role = roleDao.save(getDocument());
            if (role == null) {
                addMessage(null, MSG_CANT_SAVE);
            } else {
                setDocument(role);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            e.printStackTrace();
        }
        return result;
    }

    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<>();
        Collections.addAll(result, RoleType.values());
        return result;
    }
}