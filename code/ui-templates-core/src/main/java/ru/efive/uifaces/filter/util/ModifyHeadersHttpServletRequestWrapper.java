package ru.efive.uifaces.filter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * The HttpServletRequestWrapper for easier modifying of request's headers.
 * The headers names are assumed as case-insensitive.
 *
 * @author Pavel Porubov
 */
public class ModifyHeadersHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Collection<String> removedHeaders;
    private Map<String, Collection<String>> addedHeaders;

    /**
     * Constructs a request object wrapping the given {@code request}
     * with removed headers secified by {@code removedHeaders} and added headers specified by {@code addedHeaders}.
     * @param removedHeaders headers to remove
     * @param addedHeaders headers to add
     * @param request the request to wrap
     */
    public ModifyHeadersHttpServletRequestWrapper(Collection<String> removedHeaders,
            Map<String, Collection<String>> addedHeaders, HttpServletRequest request) {
        super(request);
        this.removedHeaders = new HashSet<String>(removedHeaders.size());
        for (String hn : removedHeaders) {
            this.removedHeaders.add(hn.toLowerCase());
        }
        this.addedHeaders = new HashMap<String, Collection<String>>(addedHeaders.size());
        for (Map.Entry<String, Collection<String>> hnv : addedHeaders.entrySet()) {
            String hn = hnv.getKey().toLowerCase();
            Collection<String> hv = this.addedHeaders.get(hn);
            if (hv == null) {
                hv = new ArrayList<String>();
                this.addedHeaders.put(hn, hv);
            }
            hv.addAll(hnv.getValue());
        }
    }

    /**
     * Constructs a request object wrapping the given {@code request}
     * with removed headers secified by {@code removedHeaders} and added headers specified by {@code addedHeaders}.
     * @param removedHeaders headers to remove
     * @param addedHeaders headers to add
     * @param request the request to wrap
     */
    public ModifyHeadersHttpServletRequestWrapper(String[] removedHeaders,
            Map<String, Collection<String>> addedHeaders, HttpServletRequest request) {
        this(Arrays.asList(removedHeaders), addedHeaders, request);
    }

    /**
     * Constructs a request object wrapping the given {@code request}
     * with removed headers secified by {@code removedHeaders} and added headers specified by {@code addedHeaders}.
     * @param removedHeaders headers to remove
     * @param addedHeaders headers to add. The even elements of the array should contain header's names,
     * the odd elements of the array should contain header's values.
     * @param request the request to wrap
     */
    public ModifyHeadersHttpServletRequestWrapper(String[] removedHeaders, String[] addedHeaders,
            HttpServletRequest request) {
        super(request);
        this.removedHeaders = new HashSet<String>(removedHeaders.length);
        for (String hn : removedHeaders) {
            this.removedHeaders.add(hn.toLowerCase());
        }
        this.addedHeaders = new HashMap<String, Collection<String>>((addedHeaders.length / 2)
                + (addedHeaders.length % 2));
        for (int i = 0; i < addedHeaders.length; i += 2) {
            String hn = addedHeaders[i].toLowerCase();
            Collection<String> hv = this.addedHeaders.get(hn);
            if (hv == null) {
                hv = new ArrayList<String>();
                this.addedHeaders.put(hn, hv);
            }
            hv.add(i < (addedHeaders.length - 1) ? addedHeaders[i + 1] : "");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> headerNames = super.getHeaderNames();
        Collection<String> res = addedHeaders != null ? new HashSet<String>(addedHeaders.keySet())
                : new HashSet<String>();
        while (headerNames.hasMoreElements()) {
            String hn = headerNames.nextElement();
            if (removedHeaders == null || !removedHeaders.contains(hn)) {
                res.add(hn);
            }
        }
        return EnumerationFactory.newInstance(res);
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<String> getHeaders(String name) {
        name = name.toLowerCase();
        if (removedHeaders != null && removedHeaders.contains(name)) {
            if (addedHeaders != null && addedHeaders.containsKey(name)) {
                return EnumerationFactory.newInstance(addedHeaders.get(name));
            } else {
                return null;
            }
        } else {
            return super.getHeaders(name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getHeader(String name) {
        name = name.toLowerCase();
        if (removedHeaders != null && removedHeaders.contains(name)) {
            if (addedHeaders != null && addedHeaders.containsKey(name)) {
                return addedHeaders.get(name).iterator().next();
            } else {
                return null;
            }
        } else {
            return super.getHeader(name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getDateHeader(String name) {
        name = name.toLowerCase();
        if (removedHeaders != null && removedHeaders.contains(name)) {
            return -1;
        } else {
            return super.getDateHeader(name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getIntHeader(String name) {
        name = name.toLowerCase();
        if (removedHeaders != null && removedHeaders.contains(name)) {
            if (addedHeaders != null && addedHeaders.containsKey(name)) {
                try {
                    return Integer.parseInt(addedHeaders.get(name).iterator().next());
                } catch (NoSuchElementException ex) {
                    throw new NumberFormatException();
                }
            } else {
                return -1;
            }
        } else {
            return super.getIntHeader(name);
        }
    }
}
