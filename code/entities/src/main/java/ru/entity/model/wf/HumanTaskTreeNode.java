package ru.entity.model.wf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import ru.entity.model.mapped.Document;

/**
 * Вершина дерева согласования (шаг процесса согласования)
 */
@Entity
@Table(name = "wf_task_tree_nodes")
public class HumanTaskTreeNode extends Document {

    public void setParentNode(HumanTaskTreeNode parentNode) {
        this.parentNode = parentNode;
    }

    public HumanTaskTreeNode getParentNode() {
        return parentNode;
    }

    public void setChildNodes(List<HumanTaskTreeNode> childNodes) {
        this.childNodes = childNodes;
    }

    public List<HumanTaskTreeNode> getChildNodes() {
        return childNodes;
    }

    public List<HumanTaskTreeNode> getChildNodeList() {
        List<HumanTaskTreeNode> result = new ArrayList<HumanTaskTreeNode>();
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
            childNodes = new ArrayList<HumanTaskTreeNode>();
        }
        childNodes.add(node);
    }

    public void setTasks(List<HumanTask> tasks) {
        this.tasks = tasks;
    }

    public List<HumanTask> getTasks() {
        return tasks;
    }

    public List<HumanTask> getTaskList() {
        List<HumanTask> result = new ArrayList<HumanTask>();
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
            tasks = new ArrayList<HumanTask>();
        }
        tasks.add(task);
    }

    public void setGrouping(int grouping) {
        this.grouping = grouping;
    }

    public int getGrouping() {
        return grouping;
    }

    @Transient
    public boolean isParentNode() {
        return childNodes != null && childNodes.size() > 0;
    }

    @Transient
    public boolean isGroupedNode() {
        return tasks != null && tasks.size() > 1;
    }


    /**
     * Родительская вершина дерева согласования
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private HumanTaskTreeNode parentNode;

    /**
     * Вершины дерева согласования - потомки
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_task_tree_child_nodes",
            joinColumns = {@JoinColumn(name = "node_id")},
            inverseJoinColumns = {@JoinColumn(name = "child_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HumanTaskTreeNode> childNodes;

    /**
     * Задачи на согласование
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_task_tree_node_tasks",
            joinColumns = {@JoinColumn(name = "node_id")},
            inverseJoinColumns = {@JoinColumn(name = "task_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HumanTask> tasks;

    /**
     * Позиция вершины в дереве
     */
    @Transient
    private int grouping;

    private static final long serialVersionUID = 4454645793646376490L;
}