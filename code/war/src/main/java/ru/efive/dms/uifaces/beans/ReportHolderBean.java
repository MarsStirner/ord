package ru.efive.dms.uifaces.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
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
public class ReportHolderBean {
    private static final Logger log = LoggerFactory.getLogger(ReportHolderBean.class);

    @Autowired
    @Qualifier("reportService")
    private ReportService reportService;

    @Autowired
    @Qualifier("reportDao")
    private ReportDao reportDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;


    /**
     * Поле, где будет храниться сам документ
     */
    private Report document;

    private Integer id;

    private Map<String, Object> result;

    private State state;


    public List<ReportParameter> getReportParameterList() {
        return document.getParameters().stream().sorted(Comparator.comparingInt(ReportParameter::getSort)).collect(Collectors.toList());
    }

    @PreDestroy
    public void destroy() {
        log.info("Bean destroyed");
    }


    public void load() {
        final User currentUser = authData.getAuthorized();
        log.info("Open Report[{}] by user[{}]", id, currentUser.getId());
        final Report document = reportDao.get(id);
        if (document == null) {
            log.warn("Report[{}] not found", id);
            setDocumentNotFound();
        } else {
            setDocument(document);
            setState(State.EDIT);
            result = new HashMap<>();
        }
    }

    public boolean fillAndShow() {
        final Report report = getDocument();
        Map<String, Object> reportParameters = getResult();
        log.info("Start generate report[{}] \'{}\'", report.getId(), report.getDisplayName());
        reportParameters.forEach((key, value) -> log.debug("ReportParameter[{}]={}", key, value));
        if (validate(report.getParameters(), reportParameters)) {
            try {
                reportService.fillAndShowReport(report, reportParameters);
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

    public boolean isCreateState() {
        return State.CREATE.equals(state);
    }

    public boolean isEditState() {
        return State.EDIT.equals(state);
    }

    public boolean isViewState() {
        return State.VIEW.equals(state);
    }

    public boolean isErrorState() {
        return State.ERROR.equals(state);
    }

    private void setState(final State state) {
        log.info("Document state changed from {} to {}", this.state, state);
        this.state = state;
    }

    /**
     * Стандартные действия когда документ не был найден
     */
    private void setDocumentNotFound() {
        setState(State.ERROR);
        MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
    }

    /**
     * Стандартные действия когда документ не был найден
     */
    private void setDocumentDeleted() {
        setState(State.ERROR);
        MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
    }


    public Report getDocument() {
        return document;
    }

    public void setDocument(Report document) {
        this.document = document;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}