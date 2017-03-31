package ru.efive.uifaces.data;

/**
 * Interface to be implemented by all documents.
 *
 * @param <T> type of id of document.
 * @author Ramil_Habirov
 */
public interface Document<T> {

    /**
     * Returns id of document.
     *
     * @return id of document.
     */
    T getId();
}
