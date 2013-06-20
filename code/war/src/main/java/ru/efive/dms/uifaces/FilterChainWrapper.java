package ru.efive.dms.uifaces;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public class FilterChainWrapper implements FilterChain {

//~ Instance variables ***************************************************************

    /**
     * The original filter chain.
     */
    private FilterChain chain;
    /**
     * If the SPNEGO filter aborted, then it stays false.
     */
    boolean chainCalled = false;
    /**
     * The original, unwrapped response.
     */
    private HttpServletResponse httpServletResponse;

//~ Constructors *********************************************************************

    /**
     * Creates a new FilterChainWrapper object.
     *
     * @param chain    The original filter chain.
     * @param response The original response.
     */
    public FilterChainWrapper(FilterChain chain, HttpServletResponse response) {
        this.chain = chain;
        httpServletResponse = response;
    }

//~ Methods **************************************************************************

    /**
     * @see javax.servlet.FilterChain#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws IOException, ServletException {
// Pass httpServletResponse, not the dummy servletResponse!
        chain.doFilter(servletRequest, httpServletResponse);

        chainCalled = true;
    }

    /**
     * Getter for the chain called property.
     *
     * @return The value of the chain called property.
     */
    public boolean isChainCalled() {
        return chainCalled;
    }
}    // end class FilterChainWrapper