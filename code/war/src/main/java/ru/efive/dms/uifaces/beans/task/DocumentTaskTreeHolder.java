package ru.efive.dms.uifaces.beans.task;

import org.apache.axis.utils.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.Task;
import ru.util.ApplicationHelper;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ru.efive.dms.util.ApplicationDAONames.TASK_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 22.10.2014, 15:20 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для содержания дерева поручений для конкретного документа<br>
 */
@Named("documentTaskTree")
@ConversationScoped
public class DocumentTaskTreeHolder extends AbstractDocumentTreeHolderBean<Task> {
    private static final Logger logger = LoggerFactory.getLogger("TASK");


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private String rootDocumentId;

    public String getRootDocumentId() {
        return rootDocumentId;
    }

    public void setRootDocumentId(String rootDocumentId) {
        this.rootDocumentId = rootDocumentId;
    }

    /**
     * Переопределяемый метод для загрузки списка документов, которые будут использоваться при построении дерева
     *
     * @param pagination
     * @return список документов
     */
    @Override
    protected List<Task> loadDocuments(Pagination pagination) {
        if (StringUtils.isEmpty(rootDocumentId)) {
            return new ArrayList<Task>(0);
        } else if(rootDocumentId.startsWith("task")){
            logger.debug("Start loading Tasks on \"{}\"", rootDocumentId);
            final Integer parentId = ApplicationHelper.getIdFromUniqueIdString(rootDocumentId);
            if(parentId != null) {
                return sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getChildrenTaskByParentId(parentId, false);
            } else {
                logger.warn("ParentId is not match pattern!");
                return new ArrayList<Task>(0);
            }
        }
        logger.debug("Start loading Tasks on \"{}\"", rootDocumentId);
        return sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getTaskListByRootDocumentId(rootDocumentId, false);
    }

    /**
     * Переопределяемый метод для построения дерева из списка документов
     *
     * @param documents список документов, по которым надо построить дерево
     * @return корневой элемент дерева
     */
    @Override
    protected DefaultTreeNode constructTreeFromDocumentList(List<Task> documents) {
        final DefaultTreeNode root = new DefaultTreeNode("ROOT", null);
        if (!documents.isEmpty()) {
            logger.debug("Start construct tree from {} tasks", documents.size());
            if (logger.isTraceEnabled()) {
                for (Task task : documents) {
                    logger.trace("{}", task);
                }
            }
            final HashMap<Integer, DefaultTreeNode> taskMap = new HashMap<Integer, DefaultTreeNode>(documents.size());
            for (Task document : documents) {
                taskMap.put(document.getId(), new DefaultTreeNode("task", document, null));
            }
            for (Map.Entry<Integer, DefaultTreeNode> entry : taskMap.entrySet()) {
                final DefaultTreeNode node = entry.getValue();
                final Task data = (Task) node.getData();
                if (data.getParent() != null) {
                    final DefaultTreeNode parentNode = taskMap.get(data.getParent().getId());
                    if(parentNode != null) {
                        node.setParent(parentNode);
                        parentNode.getChildren().add(node);
                    } else {
                        logger.warn("Cannot find parentNode[{}] in list. Add to Root", data.getParent().getId());
                        node.setParent(root);
                        root.getChildren().add(node);
                    }
                } else {
                    node.setParent(root);
                    root.getChildren().add(node);
                }
            }
        }
        return root;
    }

    /**
     * Инициализация ранжирования по страницам
     *
     * @return Изначальный режим ранжирования
     */
    @Override
    protected Pagination initPagination() {
        return new Pagination(0, -1, -1); //ALL
    }

    /**
     * Получить общее кол-во документов, подходящих запросу
     *
     * @return общее кол-во документов
     */
    @Override
    protected int getTotalCount() {
        return 0;
    }
}
