package ru.efive.dms.uifaces.beans.task;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentTreeHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.util.ApplicationHelper;
import ru.util.Node;

import java.util.List;
import java.util.stream.Stream;

/**
 * Author: Upatov Egor <br>
 * Date: 22.10.2014, 15:20 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для содержания дерева поручений для конкретного документа<br>
 */
@ViewScopedController("documentTaskTree")
public class DocumentTaskTreeHolder extends AbstractDocumentTreeHolderBean<Task> {

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
    public TreeNode loadTree() {
      return loadTree(rootDocumentId);
    }

    @Transactional("ordTransactionManager")
    private TreeNode loadTree(String rootDocumentId) {
        final Node<Task> rootNode = new Node<>();
        if (StringUtils.isNotEmpty(rootDocumentId)) {
            if (StringUtils.startsWith(rootDocumentId, "TASK_")) {
                final Integer taskId = ApplicationHelper.getIdFromUniqueIdString(rootDocumentId);
                if (taskId != null) {
                    final Task startTask = taskDao.get(taskId);
                    rootNode.setData(startTask);
                    taskDao.fetchTreeRecursive(startTask.getRootDocumentId(), rootNode, 0);
                } else {
                    log.error("loadTree(): called with non-saved TASK!");
                }
            } else {
                taskDao.fetchTreeRecursive(rootDocumentId, rootNode, 0);
            }
            if (log.isDebugEnabled()) {
                log.debug("TaskTree:\n{}", printTreeToString(rootNode, ""));
            }
        } else {
            log.warn("loadTree(): called with empty rootDocumentId!");
        }
        return convertToTreeNode(rootNode);
    }


    private String printTreeToString(Node<Task> rootNode, final String prefix) {
        Task rootData = rootNode.getData();
        final StringBuilder sb = new StringBuilder(prefix).append("├──");
        if(rootData != null){
            sb.append(rootData.getId()).append("[").append(rootData.getRegistrationNumber()).append("]");
        } else {
            sb.append("ROOT");
        }
        sb.append('\n');
        rootNode.getChildren().forEach(x -> sb.append(printTreeToString(x, prefix + "│   ")));
        return sb.toString();
    }


    private TreeNode convertToTreeNode(Node<Task> node) {
        final TreeNode result = new DefaultTreeNode(node.getData());
        result.setExpanded(true);
        result.setSelectable(true);
        result.setType(node.getData() != null ? "task" : "root");
        result.setRowKey(node.getData() != null ? String.valueOf(node.getData().getId()) : "root");
        for (Node<Task> x : node.getChildren()) {
            final TreeNode subNode = convertToTreeNode(x);
            subNode.setParent(result);
            result.getChildren().add(subNode);
        }
        return result;
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

    public List<Task> getFlatList(String rootId, boolean showDeleted) {
        return taskDao.getTaskListByRootDocumentId(rootId, showDeleted);
    }
}
