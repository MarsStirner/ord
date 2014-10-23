package ru.efive.uifaces.bean;

import org.primefaces.model.DefaultTreeNode;
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
//TODO Сортировки настраиваемые
//TODO Группировка + сортировка переопределяемая
public abstract class AbstractDocumentTreeHolderBean<D extends Serializable> implements Serializable {

    //////////////////////// ABSTRACT METHODS START ///////////////////////////////////////////////////////////////////

    /**
     * Переопределяемый метод для загрузки списка документов, которые будут использоваться при построении дерева
     *
     * @return список документов
     */
    protected abstract List<D> loadDocuments(final Pagination pagination);

    /**
     * Переопределяемый метод для построения дерева из списка документов
     *
     * @param documents список документов, по которым надо построить дерево
     * @return корневой элемент дерева
     */
    protected abstract DefaultTreeNode constructTreeFromDocumentList(final List<D> documents);

    /**
     * Инициализация ранжирования по страницам
     *
     * @return Изначальный режим ренжирования
     */
    protected abstract Pagination initPagination();

    /**
     * Получить общее кол-во документов, подходящих запросу
     *
     * @return общее кол-во документов
     */
    protected abstract int getTotalCount();

    ////////////////////// ABSTRACT METHODS END ////////////////////////////////////////////////////////////////////////

    /**
     * Флаг иницаилизации бина
     */
    private boolean initialized = false;

    /**
     * корневой элемент дерева
     */
    private DefaultTreeNode rootNode;

    /**
     * Документы, которые используются в дереве
     */
    private List<D> documents;

    /**
     * Текущая глубина узла при сворачивании\ разворачивании дерева
     */
    private int depth = 0;

    /**
     * Ограничение на максимальную глубину узла при сворачивании\разворачивании
     */
    private static final int MAX_RECURSIVE_DEPTH = 10;

    private Pagination pagination;
    private String pageToGo;
    private int pageSizeToSelect;

    @PostConstruct
    private void init() {
        this.pagination = initPagination();
        this.documents = loadDocuments(pagination);
        this.rootNode = constructTreeFromDocumentList(documents);
        this.initialized = true;
        adjustUserInputs();
    }


    /////////////////// PUBLIC METHODS /////////////////////////////////////////////////////////////////////////////////

    /**
     * Обновление данных в бине - новая загрузка документов и построение дерева
     */
    public void refresh() {
        if (!initialized) {
            init();
        } else {
            this.documents = loadDocuments(pagination);
            this.rootNode = constructTreeFromDocumentList(documents);
        }
        adjustUserInputs();
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
    public DefaultTreeNode getRoot() {
        return rootNode;
    }

    public List<D> getDocuments() {
        return documents;
    }

    private void adjustUserInputs() {
        pageToGo = String.valueOf(pagination.getPageOffset() + 1);
        pageSizeToSelect = pagination.getPageSize();
    }

    /**
     * Получить текущее ранжирование по страницам
     *
     * @return текущее ранжирование по страницам
     */
    public Pagination getPagination() {
        return pagination;
    }

    public void changePageSize(int pageSize) {
        pagination = new Pagination(0, getTotalCount(),
                Math.min(Math.max(Pagination.MIN_PAGE_SIZE, pageSize), Pagination.MAX_PAGE_SIZE));
        refresh();
    }

    // 0-based
    public void changePageOffset(int pageOffset) {
        pagination = new Pagination(pageOffset * pagination.getPageSize(), getTotalCount(), pagination.getPageSize());
        refresh();
    }

    public void changeOffset(int offset) {
        pagination = new Pagination(offset, getTotalCount(), pagination.getPageSize());
        refresh();
    }

    public void goToPage() {
        try {
            int page = Integer.parseInt(pageToGo);
            changePageOffset(page - 1);
        } catch (NumberFormatException ex) {
            // Nothing required
            System.out.println("User enter non-number as page to go parameter (" + ex.getMessage() + ").");
        }
    }

    public void selectPageSize() {
        changePageSize(pageSizeToSelect);
    }

    public int getPageSizeToSelect() {
        return pageSizeToSelect;
    }

    public void setPageSizeToSelect(int pageSizeToSelect) {
        this.pageSizeToSelect = pageSizeToSelect;
    }

    // 1-based
    public String getPageToGo() {
        return pageToGo;
    }

    // 1-based
    public void setPageToGo(String pageToGo) {
        this.pageToGo = pageToGo;
    }
}
