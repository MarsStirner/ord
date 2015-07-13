package ru.efive.dms.uifaces.beans.task;

import org.apache.axis.utils.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.TaskDAOImpl;
import ru.util.ApplicationHelper;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.TASK_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 22.10.2014, 15:20 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для содержания дерева поручений для конкретного документа<br>
 */
@Named("documentTaskTree")
@ViewScoped
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
     * @return список документов
     */
    @Override
    protected List<Task> loadDocuments() {
        if (StringUtils.isEmpty(rootDocumentId)) {
            return new ArrayList<>(0);
        } else if(rootDocumentId.startsWith("task")){
            logger.debug("Start loading Tasks on \"{}\"", rootDocumentId);
            final Integer parentId = ApplicationHelper.getIdFromUniqueIdString(rootDocumentId);
            if(parentId != null) {
                return sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getChildrenTaskByParentId(parentId, false);
            } else {
                logger.warn("ParentId is not match pattern!");
                return new ArrayList<>(0);
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
    protected TreeNode constructTreeFromDocumentList(List<Task> documents) {
        final DefaultTreeNode root = new DefaultTreeNode("ROOT", null);
        logger.debug("Start construct tree from {} tasks", documents.size());
        if (!documents.isEmpty()) {
            if (logger.isTraceEnabled()) {
                for (Task task : documents) {
                    logger.trace("{}", task);
                }
            }
            //ORD-40 Так как мы работаем с сортированным списком - то перемешивать на основе хэша его не надо
            final HashMap<Integer, TreeNode> taskMap = new LinkedHashMap<>(documents.size());
            for (Task document : documents) {
                taskMap.put(document.getId(), new DefaultTreeNode("task", document, null));
            }
            for (Map.Entry<Integer, TreeNode> entry : taskMap.entrySet()) {
                final TreeNode node = entry.getValue();
                node.setSelectable(true);
                final Task data = (Task) node.getData();
                if (data.getParent() != null) {
                    final TreeNode parentNode = taskMap.get(data.getParent().getId());
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
}
