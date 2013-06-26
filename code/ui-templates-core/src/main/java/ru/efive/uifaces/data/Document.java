package ru.efive.uifaces.data;

/**
 * Interface to be implemented by all documents.
 * 
 * @author Ramil_Habirov
 * 
 * @param <T>
 *            type of id of document.
 */
public interface Document<T> {

    /**
     * Returns id of document.
     * 
     * @return id of document.
     */
    public T getId();
}
