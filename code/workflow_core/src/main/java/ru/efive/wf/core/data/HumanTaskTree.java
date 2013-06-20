package ru.efive.wf.core.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import ru.efive.sql.entity.document.Document;

/**
 * Шаблон дерева задач на согласование
 */
@Entity
@Table(name = "wf_task_trees")
public class HumanTaskTree extends Document {

    public void setRootNodes(List<HumanTaskTreeNode> rootNodes) {
        this.rootNodes = rootNodes;
    }

    public List<HumanTaskTreeNode> getRootNodes() {
        return rootNodes;
    }

    public List<HumanTaskTreeNode> getRootNodeList() {
        List<HumanTaskTreeNode> result = new ArrayList<HumanTaskTreeNode>();
        result.addAll(rootNodes);
        Collections.sort(result, new Comparator<HumanTaskTreeNode>() {
            @Override
            public int compare(HumanTaskTreeNode o1, HumanTaskTreeNode o2) {
                return o1.getId() - o2.getId();
            }
        });
        return result;
    }

    public List<HumanTaskTreeNode> getHumanTaskTree() {
        if (humanTaskTree == null) {
            loadHumanTaskTree();
        }
        return humanTaskTree;
    }

    public List<HumanTaskTreeNode> loadHumanTaskTree() {
        humanTaskTree = new ArrayList<HumanTaskTreeNode>();
        try {
            int grouping = 0;
            if (rootNodes != null) {
                List<HumanTaskTreeNode> rootNodeList = getRootNodeList();
                for (HumanTaskTreeNode rootNode : rootNodeList) {
                    rootNode.setGrouping(grouping);
                    rootNode.getTaskList();
                    humanTaskTree.add(rootNode);
                    //System.out.println("Root node: " + rootNode.getId() + ", count of tasks: " + rootNode.getTaskList().size() + ", grouped: " + rootNode.isGroupedNode());
                    List<HumanTaskTreeNode> subTree = formHumanTaskSubTree(rootNode, grouping + 1);
                    humanTaskTree.addAll(subTree);
                }
            }
        } catch (Exception e) {
            logger.error("Exception while forming tree", e);
            humanTaskTree = new ArrayList<HumanTaskTreeNode>();
        }
        return humanTaskTree;
    }

    public void addRootNode(HumanTaskTreeNode node) {
        if (rootNodes == null) {
            rootNodes = new ArrayList<HumanTaskTreeNode>();
        }
        rootNodes.add(node);
    }

    private List<HumanTaskTreeNode> formHumanTaskSubTree(HumanTaskTreeNode rootNode, int baseGrouping) {
        List<HumanTaskTreeNode> result = new ArrayList<HumanTaskTreeNode>();
        try {
            if (rootNode.getChildNodes() != null) {
                List<HumanTaskTreeNode> childNodeList = rootNode.getChildNodeList();
                for (HumanTaskTreeNode childNode : childNodeList) {
                    childNode.setGrouping(baseGrouping);
                    childNode.getTaskList();
                    result.add(childNode);
                    //System.out.println("Child node: " + childNode.getId() + ", count of tasks: " + childNode.getTaskList().size() + ", grouped: " + childNode.isGroupedNode());
                    if (childNode.getChildNodes() != null && childNode.getChildNodes().size() > 0) {
                        List<HumanTaskTreeNode> subTree = formHumanTaskSubTree(childNode, baseGrouping + 1);
                        if (subTree != null && subTree.size() > 0) {
                            result.addAll(subTree);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception while forming tree", e);
        }
        return result;
    }


    /**
     * Вершины дерева согласования 1 уровня
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_task_tree_root_nodes",
            joinColumns = {@JoinColumn(name = "tree_id")},
            inverseJoinColumns = {@JoinColumn(name = "node_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HumanTaskTreeNode> rootNodes;

    @Transient
    private List<HumanTaskTreeNode> humanTaskTree;


    private static final Logger logger = Logger.getLogger(HumanTaskTree.class);

    private static final long serialVersionUID = 29608759242909653L;
}