package ru.efive.uifaces.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 * The filter processes multipart/form-data requests for uploading files.
 * <br>The filter intercepts such requests, extracts file data, invokes registered upload handler with that data,
 * collects remaining request data and chains that request through other registered filters and servlets.
 * <br>The filter is configured by specifying of init parameters:
 * <ul>
 * <li>UPLOAD_MAX_SIZE - maximum allowed size of request. By default is {@link UploadFilter#DEFAULT_MAX_SIZE}.</li>
 * </ul>
 *
 * @author Pavel Porubov
 */
public class UploadFilter extends AbstractFilter {

    /**
     * Init parameter's name.
     */
    public static final String UPLOAD_MAX_SIZE_PARAMETER = "UPLOAD_MAX_SIZE";
    /**
     * The default value of {@code UPLOAD_MAX_SIZE} init parameter. Equals to 1073741824.
     */
    public static final long DEFAULT_MAX_SIZE = 1073741824;
    private static final Pattern FILE_FIELD_PATTERN = Pattern.compile("^(.+)-\\d+$");
    private static final String FILE_TOO_LARGE = "Uploaded file too large";
    private long maxSize = DEFAULT_MAX_SIZE;

    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Constructs uninitialized filter.
     */
    public UploadFilter() {
    }

    private ServletRequest processUploadRequest(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hresp = (HttpServletResponse) response;
        if (hreq.getCharacterEncoding() == null || hreq.getCharacterEncoding().isEmpty()) {
            hreq.setCharacterEncoding(DEFAULT_ENCODING);
        }
        if (ServletFileUpload.isMultipartContent(hreq)) {
            final Map<String, ArrayList<String>> paramtersMap = new HashMap<String, ArrayList<String>>();

            long sz = Long.parseLong(hreq.getHeader("content-length"));
            if (sz > maxSize) {
                hresp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                throw new UploadException(FILE_TOO_LARGE);
            }

            try {
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iter = upload.getItemIterator(hreq);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    if (item.isFormField()) {
                        String pv = Streams.asString(item.openStream(), DEFAULT_ENCODING);
                        ArrayList<String> pvs = paramtersMap.get(item.getFieldName());
                        if (pvs == null) {
                            pvs = new ArrayList<String>();
                        }
                        pvs.add(pv);
                        paramtersMap.put(item.getFieldName(), pvs);
                    } else {
                        String fieldName = item.getFieldName();
                        Matcher m = FILE_FIELD_PATTERN.matcher(fieldName);
                        if (!m.matches()) {
                            continue;
                        }
                        String fileName = item.getName();
                        if (fileName == null || fileName.isEmpty()) {
                            continue;
                        }
                        String fileUploadId = m.group(1);
                        Object uploadHandler = hreq.getSession().getAttribute(fileUploadId);
                        if (uploadHandler != null && uploadHandler instanceof UploadHandler) {
                            ((UploadHandler) uploadHandler).handleUpload(new UploadInfo(hreq, hresp,
                                    fieldName, new File(fileName).getName(), item.openStream()));
                        }
                    }
                }
            } catch (FileUploadException ex) {
                throw new IOException(ex);
            }

            return new HttpServletRequestWrapper(hreq) {

                final Map<String, String[]> paramtersMapImmutable;

                {
                    Map<String, String[]> pm = new HashMap<String, String[]>();
                    for (Map.Entry<String, ArrayList<String>> pme : paramtersMap.entrySet()) {
                        pm.put(pme.getKey(), pme.getValue().toArray(new String[0]));
                    }
                    paramtersMapImmutable = pm;
                }

                @Override
                public Map<String, String[]> getParameterMap() {
                    return paramtersMapImmutable;
                }

                @Override
                public String getParameter(String name) {
                    String[] pv = paramtersMapImmutable.get(name);
                    return pv != null && pv.length > 0 ? pv[0] : null;
                }

                @Override
                public Enumeration<String> getParameterNames() {
                    return new Enumeration<String>() {

                        Iterator<String> parameterNames = paramtersMapImmutable.keySet().iterator();

                        @Override
                        public boolean hasMoreElements() {
                            return parameterNames.hasNext();
                        }

                        @Override
                        public String nextElement() {
                            return parameterNames.next();
                        }
                    };
                }

                @Override
                public String[] getParameterValues(String name) {
                    return paramtersMapImmutable.get(name);
                }
            };
        } else {
            return request;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ServletRequest requestProcessed = processUploadRequest(request, response);

        try {
            chain.doFilter(requestProcessed, response);
        } catch (Throwable t) {
            if (t instanceof ServletException) {
                throw (ServletException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            sendProcessingError(t, response);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfig) {
        super.init(filterConfig);
        if (filterConfig != null) {
            try {
                maxSize = Long.parseLong(filterConfig.getInitParameter(UPLOAD_MAX_SIZE_PARAMETER));
            } catch (NumberFormatException ex) {
            }
        }
    }
}
