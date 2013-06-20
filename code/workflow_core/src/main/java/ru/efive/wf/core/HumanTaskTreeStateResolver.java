package ru.efive.wf.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.wf.core.data.HumanTask;
import ru.efive.wf.core.data.HumanTaskTree;
import ru.efive.wf.core.data.HumanTaskTreeNode;

public class HumanTaskTreeStateResolver implements java.io.Serializable {

    private ProcessedData data;
    private HumanTaskTree tree;

    private List<HumanTask> currentTaskList;

    private static final long serialVersionUID = 6677966431502017205L;

    public HumanTaskTreeStateResolver() {

    }

    public HumanTaskTreeStateResolver(ProcessedData data, HumanTaskTree tree) {
        this.data = data;
        this.tree = tree;
    }

    public HumanTaskTree getHumanTaskTree() {
        return tree;
    }

    public void setHumanTaskTree(HumanTaskTree tree) {
        this.tree = tree;
    }

    public ProcessedData getProcessedData() {
        return data;
    }

    public void setProcessedData(ProcessedData data) {
        this.data = data;
    }


    public boolean isProcessed() {
        boolean result = true;
        try {
            if (tree.getRootNodes() != null) {
                for (HumanTaskTreeNode rootNode : tree.getRootNodeList()) {
                    boolean process = true;
                    for (HumanTask task : rootNode.getTaskList()) {
                        if (task.getId() == DocumentStatus.DRAFT.getId()) {
                            process = false;
                            result = false;
                            if (currentTaskList == null) {
                                currentTaskList = new ArrayList<HumanTask>();
                            }
                            currentTaskList.add(task);
                        }
                    }
                    if (process) {
                        boolean subProcess = isSubTreeProcessed(rootNode);
                        if (result) result = subProcess;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isDeclined() {
        boolean result = false;
        try {
            if (tree.getRootNodes() != null) {
                for (HumanTaskTreeNode rootNode : tree.getRootNodeList()) {
                    //TODO: непонятно что за статус задачи = -1
/*					for (HumanTask task: rootNode.getTaskList()) {
/*						if (task.getStatusName() == -1) {
							return true;
						}
					}    */
                    if (isSubTreeDeclined(rootNode)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isSubTreeProcessed(HumanTaskTreeNode rootNode) {
        boolean result = true;
        try {
            if (rootNode.getChildNodes() != null) {
                for (HumanTaskTreeNode childNode : rootNode.getChildNodeList()) {
                    boolean process = true;
                    for (HumanTask task : childNode.getTaskList()) {
                        if (task.getId() == DocumentStatus.DRAFT.getId()) {
                            process = false;
                            result = false;
                            if (currentTaskList == null) {
                                currentTaskList = new ArrayList<HumanTask>();
                            }
                            currentTaskList.add(task);
                        }
                    }
                    if (process) {
                        boolean subProcess = isSubTreeProcessed(childNode);
                        if (result) result = subProcess;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isSubTreeDeclined(HumanTaskTreeNode rootNode) {
        boolean result = false;
        try {
            if (rootNode.getChildNodes() != null) {
                for (HumanTaskTreeNode childNode : rootNode.getChildNodeList()) {
                    for (HumanTask task : childNode.getTaskList()) {
                        //TODO: непонятно что за статус задачи = -1
/*					for (HumanTask task: rootNode.getTaskList()) {
/*						if (task.getStatusName() == -1) {
							return true;
						}
					}    */
                    }
                    if (isSubTreeDeclined(childNode)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<HumanTask> getCurrentTaskList() {
        return currentTaskList;
    }

    public List<HumanTask> getCurrentUniqueTaskList() {
        List<HumanTask> uniqueTaskList = new ArrayList<HumanTask>();
        try {
            Set<HumanTask> tempSet = new LinkedHashSet<HumanTask>();
            tempSet.addAll(currentTaskList);
            uniqueTaskList.addAll(tempSet);
        } catch (Exception e) {
            e.printStackTrace();
            return currentTaskList;
        }
        return uniqueTaskList;
    }

    public List<User> getCurrentExecutorList() {
        List<User> currentExecutorList = new ArrayList<User>();
        try {
            for (HumanTask task : getCurrentUniqueTaskList()) {
                currentExecutorList.add(task.getExecutor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentExecutorList;
    }
}