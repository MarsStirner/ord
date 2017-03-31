package ru.efive.uifaces.bean;

import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 09.10.2014, 18:53 <br>
 * Company: Korus Consulting IT <br>
 * Description: Абстрактный бин, в котором есть дерево (Tree) <br>
 */
public abstract class AbstractDocumentTreeHolderBean<D extends Serializable> implements Serializable {

    //////////////////////// ABSTRACT METHODS START ///////////////////////////////////////////////////////////////////

    /**
     * Ограничение на максимальную глубину узла при сворачивании\разворачивании
     */
    private static final int MAX_RECURSIVE_DEPTH = 10;
    /**
     * Флаг иницаилизации бина
     */
    private boolean initialized = false;


    ////////////////////// ABSTRACT METHODS END ////////////////////////////////////////////////////////////////////////
    /**
     * корневой элемент дерева
     */
    private TreeNode rootNode;
    /**
     * Документы, которые используются в дереве
     */
    private List<D> documents;
    /**
     * Текущая глубина узла при сворачивании\ разворачивании дерева
     */
    private int depth = 0;

    /**
     * Переопределяемый метод для загрузки списка документов, которые будут использоваться при построении дерева
     *
     * @return список документов
     */
    protected abstract List<D> loadDocuments();

    /**
     * Переопределяемый метод для построения дерева из списка документов
     *
     * @param documents список документов, по которым надо построить дерево
     * @return корневой элемент дерева
     */
    protected abstract TreeNode constructTreeFromDocumentList(final List<D> documents);

    @PostConstruct
    private void init() {
        this.documents = loadDocuments();
        this.rootNode = constructTreeFromDocumentList(documents);
        this.initialized = true;
    }


    /////////////////// PUBLIC METHODS /////////////////////////////////////////////////////////////////////////////////

    /**
     * Обновление данных в бине - новая загрузка документов и построение дерева
     */
    public void refresh() {
        if (!initialized) {
            init();
        } else {
            this.documents = loadDocuments();
            this.rootNode = constructTreeFromDocumentList(documents);
        }
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
        this.depth = 0;
        changeExpandState(rootNode, false);
    }

    /**
     * Развернуть все дерево
     */
    public void expandAll() {
        this.depth = 0;
        changeExpandState(rootNode, true);
    }

    /**
     * Установить у элемента и всех дочерних элементов(рекурсивно) опцию сворачивания
     *
     * @param node   элемент, для котрого нужно утсановить опцию (а также у всех его дочерних элементов)
     * @param option true - развернуть \ false- свернуть
     */
    private void changeExpandState(final TreeNode node, final boolean option) {
        if (depth <= MAX_RECURSIVE_DEPTH) {
            node.setExpanded(option);
            if (node.getChildCount() > 0) {
                depth++;
            }
            for (TreeNode childNode : node.getChildren()) {
                changeExpandState(childNode, option);
            }
            if (node.getChildCount() > 0) {
                depth--;
            }
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

    public List<D> getDocuments() {
        return documents;
    }


}
