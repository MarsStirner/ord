package ru.efive.dms.uifaces;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WrappedHttpServletResponse extends HttpServletResponseWrapper {
    /**
     * The status, set by the SPNEGO filter.
     */
    private int status;

    /**
     * Creates a new WrappedHttpServletResponse object.
     *
     * @param httpServletResponse The wrapped response.
     */
    public WrappedHttpServletResponse(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
    }

    /**
     * Override to catch and cache the status. We do not write it to the wrapped response
     * yet, we need to check first what the status is. We do not send back a
     * SC_INTERNAL_SERVER_ERROR.
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    @Override
    public void setStatus(int i) {
        this.status = i;
    }

    /**
     * Checks the state of the response.
     *
     * @return true if negotiation is ongoing.
     */
    public boolean isInNegotiation() {
        return status == SC_UNAUTHORIZED;
    }

    /**
     * Checks whether authentication has failed.
     *
     * @return true if negotiation has failed, permanently.
     */
    public boolean isFailed() {
        return status == SC_INTERNAL_SERVER_ERROR;
    }

    /**
     * Checks whether the request went through.
     *
     * @return true if the user is logged-in.
     */
    public boolean isSuccess() {
        return status == SC_OK;
    }

    /**
     * Call this to flush the cached status to the client immediately.
     *
     * @throws IOException if this fails.
     */
    public void flushBufferToClient()
            throws IOException {
        super.setStatus(status);
        super.setContentLength(0);

        super.flushBuffer();
    }

    /**
     * Do not allow SPNEGO to flush directly!
     *
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    @Override
    public void flushBuffer()
            throws IOException {
    }

    /**
     * We don not allow SPNEGO set the content length directly.
     *
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    @Override
    public void setContentLength(int i) {
    }
}    // end class WrappedHttpServletResponse