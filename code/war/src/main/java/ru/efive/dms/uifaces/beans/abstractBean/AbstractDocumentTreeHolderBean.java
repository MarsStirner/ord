package ru.efive.dms.uifaces.beans.abstractBean;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 09.10.2014, 18:53 <br>
 * Company: Korus Consulting IT <br>
 * Description: Абстрактный бин, в котором есть дерево (Tree) <br>
 */
public abstract class AbstractDocumentTreeHolderBean<D extends Serializable> extends AbstractLoggableBean {

    //////////////////////// ABSTRACT METHODS START ///////////////////////////////////////////////////////////////////

    /**
     * Ограничение на максимальную глубину узла при сворачивании\разворачивании
     */
    private static final int MAX_RECURSIVE_DEPTH = 10;


    ////////////////////// ABSTRACT METHODS END ////////////////////////////////////////////////////////////////////////
    /**
     * корневой элемент дерева
     */
    private TreeNode rootNode = new DefaultTreeNode();


    /**
     * Переопределяемый метод для построения дерева из списка документов
     * @return корневой элемент дерева
     */
    protected abstract TreeNode loadTree();


    /////////////////// PUBLIC METHODS /////////////////////////////////////////////////////////////////////////////////

    /**
     * Обновление данных в бине - новая загрузка документов и построение дерева
     */
    public void refresh() {
        this.rootNode = loadTree();
    }

    /**
     * Обновление данных в бине и приведение всего дерева в развернутое или свернутое состояние
     *
     * @param option true- развернуть все \ false - свернуть все
     */
    public void refresh(boolean option) {
        refresh();
        if (option) {
            expandAll();
        } else {
            collapseAll();
        }
    }

    /**
     * Свернуть все дерево
     */
    public void collapseAll() {
        changeExpandState(rootNode, false, 0);
    }

    /**
     * Развернуть все дерево
     */
    public void expandAll() {
        changeExpandState(rootNode, true, 0);
    }

    /**
     * Установить у элемента и всех дочерних элементов(рекурсивно) опцию сворачивания
     *
     * @param node   элемент, для котрого нужно утсановить опцию (а также у всех его дочерних элементов)
     * @param option true - развернуть \ false- свернуть
     */
    private void changeExpandState(final TreeNode node, final boolean option, int depth) {
        if (depth <= MAX_RECURSIVE_DEPTH) {
            node.setExpanded(option);
            node.getChildren().forEach(x-> changeExpandState(x, option, depth+1));
        } else {
            log.warn("MAX_RECURSIVE_DEPTH[{}] reached", MAX_RECURSIVE_DEPTH);
        }
    }

    /**
     * Получить корневой элемент дерева
     *
     * @return корневой элемент дерева
     */
    public TreeNode getRoot() {
        return rootNode;
    }
}