package ru.entity.model.wf;

import ru.entity.model.mapped.Document;

import javax.persistence.*;
import java.util.*;

/**
 * Вершина дерева согласования (шаг процесса согласования)
 */
@Entity
@Table(name = "wf_task_tree_nodes")
public class HumanTaskTreeNode extends Document {

    private static final long serialVersionUID = 4454645793646376490L;
    /**
     * Родительская вершина дерева согласования
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private HumanTaskTreeNode parentNode;
    /**
     * Вершины дерева согласования - потомки
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_task_tree_child_nodes",
            joinColumns = {@JoinColumn(name = "node_id")},
            inverseJoinColumns = {@JoinColumn(name = "child_id")})
    private List<HumanTaskTreeNode> childNodes;
    /**
     * Задачи на согласование
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_task_tree_node_tasks",
            joinColumns = {@JoinColumn(name = "node_id")},
            inverseJoinColumns = {@JoinColumn(name = "task_id")})
    private Set<HumanTask> tasks;
    /**
     * Позиция вершины в дереве
     */
    @Transient
    private int grouping;

    public HumanTaskTreeNode getParentNode() {
        return parentNode;
    }

    public List<HumanTaskTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<HumanTaskTreeNode> childNodes) {
        this.childNodes = childNodes;
    }

    public List<HumanTaskTreeNode> getChildNodeList() {
        List<HumanTaskTreeNode> result = new ArrayList<>();
        if (childNodes != null) {
            result.addAll(childNodes);
        }
        Collections.sort(result, new Comparator<HumanTaskTreeNode>() {
            @Override
            public int compare(HumanTaskTreeNode o1, HumanTaskTreeNode o2) {
                return o1.getId() - o2.getId();
            }
        });
        return result;
    }

    public void addChildNode(HumanTaskTreeNode node) {
        if (childNodes == null) {
            childNodes = new ArrayList<>();
        }
        childNodes.add(node);
    }

    public List<HumanTask> getTasks() {
        if (tasks != null && !tasks.isEmpty()) {
            return new ArrayList<>(tasks);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setTasks(List<HumanTask> tasks) {
        if (this.tasks != null) {
            this.tasks.clear();
            this.tasks.addAll(tasks);
        } else {
            this.tasks = new HashSet<>(tasks);
        }
    }

    public List<HumanTask> getTaskList() {
        List<HumanTask> result = new ArrayList<>();
        if (tasks != null) {
            result.addAll(tasks);
        }
        Collections.sort(result, new Comparator<HumanTask>() {
            @Override
            public int compare(HumanTask o1, HumanTask o2) {
                return o1.getId() - o2.getId();
            }
        });
        return result;
    }

    public HumanTask getFirstTask() {
        return tasks != null && tasks.size() > 0 ? getTaskList().get(0) : null;
    }

    public void addTask(HumanTask task) {
        if (tasks == null) {
            tasks = new HashSet<>();
        }
        tasks.add(task);
    }

    public int getGrouping() {
        return grouping;
    }

    public void setGrouping(int grouping) {
        this.grouping = grouping;
    }

    @Transient
    public boolean isParentNode() {
        return childNodes != null && childNodes.size() > 0;
    }

    public void setParentNode(HumanTaskTreeNode parentNode) {
        this.parentNode = parentNode;
    }

    @Transient
    public boolean isGroupedNode() {
        return tasks != null && tasks.size() > 1;
    }
}