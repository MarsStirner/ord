package ru.efive.dms.uifaces.beans;

import com.github.javaplugs.jsf.SpringScopeView;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.RegionDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.entity.model.document.ReportTemplate;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.referenceBook.Region;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ReportDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.MSG_ERROR_ON_REPORT_CREATION;

@Controller("reportTemplate")
@SpringScopeView
public class ReportTemplateHolderBean extends AbstractDocumentHolderBean<ReportTemplate> {
    //Именованный логгер (INCOMING_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("REPORT");
    @Autowired
    @Qualifier("reports")
    private ReportsManagmentBean reportsManagement;

    @Autowired
    @Qualifier("reportDao")
    private ReportDao reportDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @PreDestroy
    public void destroy() {
        LOGGER.info("Bean destroyed");
    }

    @Override
    protected boolean deleteDocument() {
        LOGGER.warn("Try to delete reportTemplate. Operation not allowed");
        return false;
    }

    @Override
    protected void initDocument(final Integer id) {
        final User currentUser = authData.getAuthorized();
        LOGGER.info("Open Report[{}] by user[{}]", id, currentUser.getId());
        final ReportTemplate document = reportDao.get(id);
        if (document == null) {
            LOGGER.warn("Report[{}] not found", id);
            setDocumentNotFound();
        } else {
            document.setStartDate(new Date());
            document.setEndDate(new Date());
            setDocument(document);
            setState(State.EDIT);
        }
    }

    @Override
    protected void initNewDocument() {
        LOGGER.warn("Try to create reportTemplate. Operation not allowed");
    }

    @Override
    protected boolean saveDocument() {
        final ReportTemplate document = getDocument();
        LOGGER.info("Start generate report[{}] \'{}\'", document.getId(), document.getDisplayName());
        if (validate(document)) {
            try {
                reportsManagement.sqlPrintReportByRequestParams(document);
                return true;
            } catch (Exception e) {
                addMessage(null, MSG_ERROR_ON_REPORT_CREATION);
                LOGGER.error("Error on create report:", e);
                return false;
            }
        } else {
            LOGGER.warn("Report params is not valid!");
            return false;
        }
    }

    private boolean validate(final ReportTemplate document) {
        return true;
    }

    @Override
    protected boolean saveNewDocument() {
        LOGGER.warn("Try to save NEW reportTemplate. Operation not allowed");
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора пользователя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePerson() {
        final ReportTemplate document = getDocument();
        final Map<String, List<String>> params = new HashMap<>();
        if (StringUtils.isNotEmpty(document.getUserGroup())) {
            if (Group.RB_CODE_MANAGERS.equals(document.getUserGroup())) {
                params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
                params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(Group.RB_CODE_MANAGERS));
            } else {
                LOGGER.warn("Report userGroup is dynamic \'\'", document.getUserGroup());
                params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(document.getUserGroup()));
            }
        }
        final User preselected = getDocument().getUser();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose user: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setUser(selected);
        }
    }

    //Выбора Региона ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRegion() {
        final Region preselected = getDocument().getRegion();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(RegionDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectRegionDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onRegionChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose region: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Region selected = (Region) result.getResult();
            getDocument().setRegion(selected);
        }
    }


}