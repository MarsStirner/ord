package ru.efive.dms.uifaces.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.report.Report;
import ru.entity.model.report.ReportParameter;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ReportDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

import static ru.efive.dms.util.message.MessageHolder.MSG_ERROR_ON_REPORT_CREATION;

@ViewScopedController("report")
public class ReportTemplateHolderBean extends AbstractDocumentHolderBean<Report> {
    @Autowired
    @Qualifier("reportService")
    private ReportService reportService;

    @Autowired
    @Qualifier("reportDao")
    private ReportDao reportDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    private Map<String, Object> result = new HashMap<>();

    public List<ReportParameter> getReportParameterList() {
        return getDocument().getParameters().stream().sorted(Comparator.comparingInt(ReportParameter::getSort)).collect(Collectors.toList());
    }

    @PreDestroy
    public void destroy() {
        log.info("Bean destroyed");
    }

    @Override
    protected boolean deleteDocument() {
        log.warn("Try to delete reportTemplate. Operation not allowed");
        return false;
    }

    @Override
    protected void initDocument(final Integer id) {
        final User currentUser = authData.getAuthorized();
        log.info("Open Report[{}] by user[{}]", id, currentUser.getId());
        final Report document = reportDao.get(id);
        if (document == null) {
            log.warn("Report[{}] not found", id);
            setDocumentNotFound();
        } else {
            setDocument(document);
            setState(State.EDIT);
        }
    }

    @Override
    protected void initNewDocument() {
        log.warn("Try to create reportTemplate. Operation not allowed");
    }

    @Override
    protected boolean saveDocument() {
        final Report report = getDocument();
        Map<String, Object> reportParameters = getResult();
        log.info("Start generate report[{}] \'{}\'", report.getId(), report.getDisplayName());
        reportParameters.forEach((key, value) -> log.debug("ReportParameter[{}]={}", key, value));
        if (validate(report.getParameters(), reportParameters)) {
            try {
                reportService.sqlPrintReportByRequestParams(report, reportParameters);
            } catch (Exception e) {
                MessageUtils.addMessage(MSG_ERROR_ON_REPORT_CREATION);
                log.error("Error on create report:", e);
                return false;
            }
            return true;
        } else {
            log.warn("Report params is not valid!");
            return false;
        }
    }


    private boolean validate(Set<ReportParameter> parameters, Map<String, Object> values) {
        return parameters.stream().filter(ReportParameter::isRequired).allMatch(x -> {
            boolean valueIsPresent = values.get(x.getName()) != null;
            if (!valueIsPresent) {
                log.warn("Required parameter [{}]-'{}' not present", x.getName(), x.getLabel());
                MessageUtils.addMessage(MessageHolder.MSG_REQUIRED_PARAMETER_NOT_SET, x.getLabel());
            }
            return valueIsPresent;
        });
    }


    @Override
    public boolean saveNewDocument() {
        log.warn("Try to save NEW reportTemplate. Operation not allowed");
        return false;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора пользователя ////////////////////////////////////////////////////////////////////////////////////////////////////
   /* public void choosePerson() {
        final Report document = getDocument();
        final Map<String, List<String>> params = new HashMap<>();
        if (StringUtils.isNotEmpty(document.getUserGroup())) {
            if (Group.RB_CODE_MANAGERS.equals(document.getUserGroup())) {
                params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
                params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(Group.RB_CODE_MANAGERS));
            } else {
                log.warn("Report userGroup is dynamic \'\'", document.getUserGroup());
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
        log.info("Choose user: {}", result);
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
        log.info("Choose region: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Region selected = (Region) result.getResult();
            getDocument().setRegion(selected);
        }
    }
*/

}