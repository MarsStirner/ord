package ru.efive.dms.uifaces.beans.task;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.uifaces.bean.AbstractDocumentTreeHolderBean;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.util.ApplicationHelper;

import java.util.*;

/**
 * Author: Upatov Egor <br>
 * Date: 22.10.2014, 15:20 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для содержания дерева поручений для конкретного документа<br>
 */
@ViewScopedController("documentTaskTree")
public class DocumentTaskTreeHolder extends AbstractDocumentTreeHolderBean<Task> {
    private static final Logger logger = LoggerFactory.getLogger("TASK");


    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;

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
    @Transactional("ordTransactionManager")
    protected List<Task> loadDocuments() {
        if (StringUtils.isEmpty(rootDocumentId)) {
            return new ArrayList<>(0);
        } else if (rootDocumentId.startsWith("task")) {
            logger.debug("Start loading Tasks on \"{}\"", rootDocumentId);
            final Integer parentId = ApplicationHelper.getIdFromUniqueIdString(rootDocumentId);
            if (parentId != null) {
                return taskDao.getChildrenTaskByParentId(parentId, false);
            } else {
                logger.warn("ParentId is not match pattern!");
                return new ArrayList<>(0);
            }
        }
        logger.debug("Start loading Tasks on \"{}\"", rootDocumentId);
        return taskDao.getTaskListByRootDocumentId(rootDocumentId, false);
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
                    if (parentNode != null) {
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

    @Override
    @Transactional("ordTransactionManager")
    public void refresh() {
        super.refresh();
    }

    @Override
    @Transactional("ordTransactionManager")
    public void refresh(boolean option) {
        super.refresh(option);
    }
}
