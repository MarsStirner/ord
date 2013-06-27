package ru.efive.uifaces.filter;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletResponse;

/**
 * Base filter
 *
 * @author Pavel Porubov
 */
public abstract class AbstractFilter implements Filter {

    protected FilterConfig filterConfig = null;

    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return (this.getClass().getName() + "()");
        }
        StringBuilder sb = new StringBuilder(this.getClass().getName() + "(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    /**
     * Outputs error message to response.
     * @param t the error
     * @param response response used to output error
     */
    protected void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);
        log(stackTrace);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Returns stacktrace as string.
     * @param t {@link Throwable} for which stacktrace should be returned
     * @return stacktrace for {@code t}
     */
    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    /** Writes the specified message to a servlet log file.
     * @param msg message
     */
    public void log(String msg) {
        if (filterConfig != null) {
            filterConfig.getServletContext().log(msg);
        } else {
            System.out.print(msg);
        }
    }
}
