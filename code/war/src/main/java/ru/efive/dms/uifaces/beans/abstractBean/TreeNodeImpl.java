package ru.efive.dms.uifaces.beans.abstractBean;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 29.06.2015, 16:15 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class TreeNodeImpl extends DefaultTreeNode {
    private List<TreeNode> children;

    public TreeNodeImpl() {
        setType(DEFAULT_TYPE);
        this.children = new ArrayList<>();
    }

    public TreeNodeImpl(Object data) {
       this();
        setData(data);
    }

    public TreeNodeImpl(Object data, TreeNode parent) {
       this(data);
        if(parent != null) {
            parent.getChildren().add(this);
        }
    }

    public TreeNodeImpl(String type, Object data, TreeNode parent) {
      this(data, parent);
        setType(type);
    }
        @Override
        public List<TreeNode> getChildren() {
            return children;
        }

    @Override
        public void setChildren(List<TreeNode> children) {
            this.children = children;
        }

    }

