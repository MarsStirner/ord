package ru.entity.model.wf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.entity.model.mapped.Document;

import javax.persistence.*;
import java.util.*;

/**
 * Шаблон дерева задач на согласование
 */
@Entity
@Table(name = "wf_task_trees")
public class HumanTaskTree extends Document {

    private static final Logger logger = LoggerFactory.getLogger("HUMAN_TASK");
    private static final long serialVersionUID = 29608759242909653L;
    /**
     * Вершины дерева согласования 1 уровня
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_task_tree_root_nodes",
            joinColumns = {@JoinColumn(name = "tree_id")},
            inverseJoinColumns = {@JoinColumn(name = "node_id")})
    private Set<HumanTaskTreeNode> rootNodes;
    @Transient
    private List<HumanTaskTreeNode> humanTaskTree;

    public List<HumanTaskTreeNode> getRootNodes() {
        if(rootNodes != null) {
            return new ArrayList<>(rootNodes);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setRootNodes(List<HumanTaskTreeNode> rootNodes) {
        if (this.rootNodes != null) {
            this.rootNodes.clear();
            this.rootNodes.addAll(rootNodes);
        } else {
            this.rootNodes = new HashSet<>(rootNodes);
        }
    }

    public List<HumanTaskTreeNode> getRootNodeList() {
        List<HumanTaskTreeNode> result = new ArrayList<>();
        result.addAll(rootNodes);
        Collections.sort(
                result, new Comparator<HumanTaskTreeNode>() {
                    @Override
                    public int compare(HumanTaskTreeNode o1, HumanTaskTreeNode o2) {
                        return o1.getId() - o2.getId();
                    }
                }
        );
        return result;
    }

    public List<HumanTaskTreeNode> getHumanTaskTree() {
        if (humanTaskTree == null) {
            loadHumanTaskTree();
        }
        return humanTaskTree;
    }

    public List<HumanTaskTreeNode> loadHumanTaskTree() {
        humanTaskTree = new ArrayList<>();
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
            humanTaskTree = new ArrayList<>();
        }
        return humanTaskTree;
    }

    public void addRootNode(HumanTaskTreeNode node) {
        if (rootNodes == null) {
            rootNodes = new HashSet<>();
        }
        rootNodes.add(node);
    }

    private List<HumanTaskTreeNode> formHumanTaskSubTree(HumanTaskTreeNode rootNode, int baseGrouping) {
        List<HumanTaskTreeNode> result = new ArrayList<>();
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
}