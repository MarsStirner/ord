package ru.efive.dms.uifaces.beans.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;
import ru.entity.model.workflow.Status;
import ru.hitsl.sql.dao.interfaces.EditableMatrixDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: Upatov Egor <br>
 * Date: 07.07.2015, 19:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@DependsOn("mappedEnumLoader")
@Controller("editableMatrix")
@Transactional("ordTransactionManager")
public class DocumentFieldEditableMatrix {
    private static final Logger logger = LoggerFactory.getLogger("EDITABLE_MATRIX");


    @Autowired
    @Qualifier("editableMatrixDao")
    private EditableMatrixDao dao;

    @Autowired
    private PlatformTransactionManager transactionManager;


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
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
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
        } finally {
            transactionManager.commit(transaction);
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

    public void loadTask() {
        logger.debug("start loading task");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.TASK.getCode());
        final List<Status> statuses = Stream.of(
                Status.DRAFT,
                Status.ON_EXECUTION,
                Status.EXECUTED,
                Status.CANCELED
        ).collect(Collectors.toList());
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.TASK.getCode());
        task = new DocumentTypeMatrix(DocumentType.TASK.getCode(), fields, statuses, matrix);
        task.toLog();
    }

    public void loadRequest() {
        logger.debug("start loading request");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.REQUEST.getCode());
        final List<Status> statuses = Stream.of(
                Status.DRAFT,
                Status.REGISTERED,
                Status.ON_EXECUTION,
                Status.EXECUTED,
                Status.IN_ARCHIVE,
                Status.OUT_ARCHIVE
        ).collect(Collectors.toList());
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.REQUEST.getCode());
        request = new DocumentTypeMatrix(DocumentType.REQUEST.getCode(), fields, statuses, matrix);
        request.toLog();
    }

    public void loadInternal() {
        logger.debug("start loading internal");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.INTERNAL.getCode());
        final List<Status> statuses = Stream.of(
                Status.DRAFT,
                Status.ON_AGREEMENT,
                Status.ON_CONSIDERATION,
                Status.REGISTERED,
                Status.ON_EXECUTION,
                Status.IN_ARCHIVE,
                Status.OUT_ARCHIVE
        ).collect(Collectors.toList());
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.INTERNAL.getCode());
        internal = new DocumentTypeMatrix(DocumentType.INTERNAL.getCode(), fields, statuses, matrix);
        internal.toLog();
    }

    public void loadOutgoing() {
        logger.debug("start loading outgoing");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.OUTGOING.getCode());
        final List<Status> statuses = Stream.of(
                Status.DRAFT,
                Status.ON_AGREEMENT,
                Status.ON_CONSIDERATION,
                Status.REGISTERED,
                Status.EXECUTED,
                Status.IN_ARCHIVE
        ).collect(Collectors.toList());
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.OUTGOING.getCode());
        outgoing = new DocumentTypeMatrix(DocumentType.OUTGOING.getCode(), fields, statuses, matrix);
        outgoing.toLog();
    }

    /**
     * Загрузить вместо старой матрицы редактируемости полей входящих документов новые данные из источника
     */
    public void loadIncoming() {
        logger.debug("start loading incoming");
        final List<EditableFieldMatrix> matrix = dao.getFieldMatrixByDocumentTypeCode(DocumentType.INCOMING.getCode());
        final List<Status> statuses = Stream.of(
                Status.DRAFT,
                Status.REGISTERED,
                Status.ON_EXECUTION,
                Status.EXECUTED,
                Status.IN_ARCHIVE,
                Status.OUT_ARCHIVE
        ).collect(Collectors.toList());
        final List<DocumentTypeField> fields = dao.getDocumentTypeFieldByDocumentTypeCode(DocumentType.INCOMING.getCode());
        incoming = new DocumentTypeMatrix(DocumentType.INCOMING.getCode(), fields, statuses, matrix);
        incoming.toLog();
    }

    /**
     * Сохранить в источник измененные записи, а затем забрать оттуда данные (reload)
     */
    //@Autowired
    public void applyAllChanges(AuthorizationData authData) {
        logger.warn("Apply matrix changes to DB");
        if (!authData.isAdministrator()) {
            logger.error("TRY TO save changes by NON-admin user [{}]. ACCESS FORBIDDEN. reload matrix from source.", authData.getAuthorized().getId());
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
        private List<Status> statuses;
        private List<EditableFieldMatrix> matrix;
        private Map<Integer, Map<String, Boolean>> fastAccessMap;

        public DocumentTypeMatrix(
                final String documentTypeCode,
                final List<DocumentTypeField> fields,
                final List<Status> statuses,
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
            if (!statusFields.isEmpty()) {
                fastAccessMap.put(lastStatus, statusFields);
            }
        }

        public List<DocumentTypeField> getFields() {
            return fields;
        }

        public List<Status> getStatuses() {
            return statuses;
        }

        public List<EditableFieldMatrix> getMatrix() {
            return matrix;
        }

        public boolean isFieldEditable(final int statusId, final String fieldName) {
            return true;
//            return fastAccessMap.get(statusId).entrySet().stream().filter(x -> fieldName.equals(x.getKey())).findFirst().map(Map.Entry::getValue).orElse(false);
        }

        protected void toLog() {
            if (logger.isDebugEnabled()) {
                logger.debug("[{}]Statuses: {}", documentTypeCode, statuses.size());
                for (Status item : statuses) {
                    logger.debug("{}:\'{}\'", item.getId(), item.getCode());
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
            return matrix.stream().filter(x -> Objects.equals(statusId, x.getStatusId()) && Objects.equals(fieldName, x.getField().getFieldCode())).findFirst().orElse(null);
        }

        public boolean isStateEditable(int status) {
            return true;
//            return fastAccessMap.get(status).entrySet().stream().anyMatch(Map.Entry::getValue);
        }
    }
}
