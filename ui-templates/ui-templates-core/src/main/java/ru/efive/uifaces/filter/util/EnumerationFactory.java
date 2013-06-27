package ru.efive.uifaces.filter.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The utility class to instantiate {@code Enumeration<E>} by some ways.
 *
 * @author Pavel Porubov
 */
public class EnumerationFactory {

    /**
     * The implementation of {@code Enumeration<E>} for {@code Iterator<E>}.
     * @param <E> elements type
     */
    private static class IteratorEnumeration<E> implements Enumeration<E> {

        private Iterator<? extends E> data;

        public IteratorEnumeration(Collection<? extends E> data) {
            this.data = data.iterator();
        }

        public IteratorEnumeration(Iterator<? extends E> data) {
            this.data = data;
        }

        @Override
        public boolean hasMoreElements() {
            return data.hasNext();
        }

        @Override
        public E nextElement() {
            return data.next();
        }
    }

    /**
     * The implementation of {@code Enumeration<E>} for array.
     * @param <E> elements type
     */
    private static class ArrayEnumeration<E> implements Enumeration<E> {

        private E[] data;
        private int i;

        public ArrayEnumeration(E[] data) {
            this.data = data;
        }

        @Override
        public boolean hasMoreElements() {
            return data != null && i < data.length;
        }

        @Override
        public E nextElement() {
            if (hasMoreElements()) {
                return data[i++];
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    /**
     * Instantiates {@code Enumeration<E>} for {@code Collection<E>} 
     * @param <E> elements type
     * @param data source collection
     * @return instance of {@code Enumeration<E>} for {@code data}
     */
    public static <E> Enumeration<E> newInstance(Collection<? extends E> data) {
        return new IteratorEnumeration<E>(data);
    }

    /**
     * Instantiates {@code Enumeration<E>} for {@code Iterator<E>} 
     * @param <E> elements type
     * @param data source iterator
     * @return instance of {@code Enumeration<E>} for {@code data}
     */
    public static <E> Enumeration<E> newInstance(Iterator<? extends E> data) {
        return new IteratorEnumeration<E>(data);
    }

    /**
     * Instantiates {@code Enumeration<E>} for array
     * @param <E> elements type
     * @param data source array
     * @return instance of {@code Enumeration<E>} for {@code data}
     */
    public static <E> Enumeration<E> newInstance(E[] data) {
        return new ArrayEnumeration<E>(data);
    }
}
