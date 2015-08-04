package ru.efive.dms.uifaces.beans.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;
import ru.hitsl.sql.dao.EditableMatrixDaoImpl;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 07.07.2015, 19:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("editableMatrix")
@ApplicationScoped
public class DocumentFieldEditableMatrix {
    private static final Logger logger = LoggerFactory.getLogger("EDITABLE_MATRIX");
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    /**
     * Yes, we can inject @SessionScoped in @ApplicationScoped
     * deal with it!
     * http://docs.jboss.org/weld/reference/latest/en-US/html/injection.html#d0e1429 (4.9 Client proxies)
     */
    @Inject
    @Named("sessionManagement")
    private SessionManagementBean sessionManagement;

    private EditableMatrixDaoImpl dao = null;
    private DocumentTypeMatrix incoming;
    private DocumentTypeMatrix outgoing;
    private DocumentTypeMatrix internal;
    private DocumentTypeMatrix request;
    private DocumentTypeMatrix task;

    public DocumentTypeMatrix getIncoming() {
        return incoming;
    }

    public DocumentTypeMatrix getOutgoing() {
        return outgoing;
    }

    public DocumentTypeMatrix getInternal() {
        return internal;
    }

    public DocumentTypeMatrix getRequest() {
        return request;
    }

    public DocumentTypeMatrix getTask() {
        return task;
    }


    /**
     * Заполнить матрицу из внешнего источника
     */
    @PostConstruct
    public void load() {
        logger.info("Start initialize ({})", this);
        dao = (EditableMatrixDaoImpl) indexManagement.getContext().getBean(ApplicationDAONames.EDITABLE_MATRIX_DAO);
        if (dao == null) {
            logger.error("DAO is not collected. Possible future errors...");
        } else {
            logger.info("DAO collected [{}]", dao);
            loadIncoming();
            loadOutgoing();
            loadInternal();
            loadRequest();
            loadTask();
            logger.info("Successful end of initialize ({})", this);
        }
    }



    /**
     * Заполнить матрицу из внешнего источника без инициализации бина
     */
    public void reload() {
        logger.info("Start reload ({})", this);
        loadIncoming();
        loadOutgoing();
        loadInternal();
        loadRequest();
        loadTask();
        logger.info("reload complete ({})", this);
    }

    private void loadTask() {
        logger.debug("start loading task");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.RB_CODE_TASK);
        final List<DocumentStatus> statuses = ru.entity.model.enums.DocumentType.getTaskStatuses();
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.RB_CODE_TASK);
        task = new DocumentTypeMatrix(DocumentType.RB_CODE_TASK, fields, statuses, matrix);
        task.toLog();
    }

    private void loadRequest() {
        logger.debug("start loading request");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.RB_CODE_REQUEST);
        final List<DocumentStatus> statuses = ru.entity.model.enums.DocumentType.getRequestDocumentStatuses();
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.RB_CODE_REQUEST);
        request = new DocumentTypeMatrix(DocumentType.RB_CODE_REQUEST, fields, statuses, matrix);
        request.toLog();
    }

    private void loadInternal() {
        logger.debug("start loading internal");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.RB_CODE_INTERNAL);
        final List<DocumentStatus> statuses = new ArrayList<>(ru.entity.model.enums.DocumentType.getInternalDocumentStatuses());
        statuses.remove(DocumentStatus.REDIRECT);
        statuses.remove(DocumentStatus.CANCEL_150);
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.RB_CODE_INTERNAL);
        internal = new DocumentTypeMatrix(DocumentType.RB_CODE_INTERNAL, fields, statuses, matrix);
        internal.toLog();
    }

    private void loadOutgoing() {
        logger.debug("start loading outgoing");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.RB_CODE_OUTGOING);
        final List<DocumentStatus> statuses = ru.entity.model.enums.DocumentType.getOutgoingDocumentStatuses();
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.RB_CODE_OUTGOING);
        outgoing = new DocumentTypeMatrix(DocumentType.RB_CODE_OUTGOING, fields, statuses, matrix);
        outgoing.toLog();
    }

    /**
     * Загрузить вместо старой матрицы редактируемости полей входящих документов новые данные из источника
     */
    public void loadIncoming() {
        logger.debug("start loading incoming");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.RB_CODE_INCOMING);
        final List<DocumentStatus> statuses = new ArrayList<>(ru.entity.model.enums.DocumentType.getIncomingDocumentStatuses());
        statuses.remove(DocumentStatus.SOURCE_DESTROY);
        statuses.remove(DocumentStatus.REDIRECT);
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.RB_CODE_INCOMING);
        incoming = new DocumentTypeMatrix(DocumentType.RB_CODE_INCOMING, fields, statuses, matrix);
        incoming.toLog();
    }

    /**
     * Сохранить в источник измененные записи, а затем забрать оттуда данные (reload)
     */
    public void applyAllChanges() {
        logger.warn("Apply matrix changes to DB");
        if(!sessionManagement.getAuthData().isAdministrator()){
            logger.error("TRY TO save changes by NON-admin user [{}]. ACCESS FORBIDDEN. reload matrix from source.", sessionManagement.getAuthData().getAuthorized().getId());
            reload();
            return;
        }
        dao.updateValues(incoming.getMatrix());
        dao.updateValues(outgoing.getMatrix());
        dao.updateValues(internal.getMatrix());
        dao.updateValues(request.getMatrix());
        dao.updateValues(task.getMatrix());
        logger.warn("Successful applied changes to DB");
        reload();
    }


    public class DocumentTypeMatrix {
        private String documentTypeCode;
        private List<DocumentTypeField> fields;
        private List<DocumentStatus> statuses;
        private List<EditableFieldMatrix> matrix;
        private Map<Integer, Map<String, Boolean>> fastAccessMap;

        public DocumentTypeMatrix(
                final String documentTypeCode,
                final List<DocumentTypeField> fields,
                final List<DocumentStatus> statuses,
                final List<EditableFieldMatrix> matrix

        ) {
            this.documentTypeCode = documentTypeCode;
            this.fields = fields;
            this.statuses = statuses;
            this.matrix = matrix;
            this.fastAccessMap = new HashMap<>(statuses.size());
            Map<String, Boolean> statusFields = new HashMap<>(0);
            int lastStatus = -1;
            for (EditableFieldMatrix item : matrix) {
                if (lastStatus != item.getStatusId()) {
                    if (!statusFields.isEmpty()) {
                        fastAccessMap.put(lastStatus, statusFields);
                    }
                    lastStatus = item.getStatusId();
                    statusFields = new HashMap<>(fields.size());
                }
                statusFields.put(item.getField().getFieldCode(), item.isEditable() && item.getField().isEditable());
            }
        }

        public List<DocumentTypeField> getFields() {
            return fields;
        }

        public List<DocumentStatus> getStatuses() {
            return statuses;
        }

        public List<EditableFieldMatrix> getMatrix() {
            return matrix;
        }

        public Map<String, Boolean> getEditableMap(final int statusId) {
            return fastAccessMap.get(statusId);
        }

        public boolean isFieldEditable(final int statusId, final String fieldName) {
            final Map<String, Boolean> map = getEditableMap(statusId);
            if (map != null) {
                return map.get(fieldName);
            } else {
                return false;
            }
        }

        protected void toLog() {
            if (logger.isDebugEnabled()) {
                logger.debug("[{}]Statuses: {}", documentTypeCode, statuses.size());
                for (DocumentStatus item : statuses) {
                    logger.debug("{}:\'{}\'", item.getId(), item.getName());
                }
                logger.debug("[{}]Fields: {}", documentTypeCode, fields.size());
                for (DocumentTypeField item : fields) {
                    logger.debug("{}:\'{}\' [{}] {}", item.getId(), item.getFieldName(), item.getFieldCode(), item.isEditable());
                }
                logger.debug("[{}]Matrix: {}", documentTypeCode, matrix.size());
                for (EditableFieldMatrix item : matrix) {
                    logger.debug("{}:\'{}\' {} {}", item.getField().getId(), item.getField().getFieldCode(), item.getStatusId(), item.isEditable());
                }
                logger.debug("[{}] Map: {}", documentTypeCode, fastAccessMap.size());
                for (Map.Entry<Integer, Map<String, Boolean>> mapES : fastAccessMap.entrySet()) {
                    logger.debug("{} : {}", mapES.getKey(), mapES.getValue());
                }
            }
        }

        public EditableFieldMatrix getItem(int statusId, String fieldName) {
            for (EditableFieldMatrix item : matrix) {
                if(item.getStatusId() == statusId && item.getField().getFieldCode().equals(fieldName)){
                    return item;
                }
            }
            return null;
        }

        public boolean isStateEditable(int status){
            for (Map.Entry<String, Boolean> entry : getEditableMap(status).entrySet()) {
                if(entry.getValue()){
                    return true;
                }
            }
            return false;
        }
    }
}
