package com.restro.filters;

import com.restro.utility.AppLogger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Last line of defense: catches anything a controller didn't handle so a
 * customer/kitchen/admin screen never shows a raw Java stack trace. Logs the
 * full exception server-side, then hands off to the container's 500
 * error-page (web.xml) which renders a clean, on-brand error screen.
 */
public class ExceptionHandlingFilter implements Filter {

    private static final AppLogger LOG = AppLogger.getLogger(ExceptionHandlingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            LOG.error("Unhandled exception on " + httpRequest.getMethod() + " " + httpRequest.getRequestURI(), e);
            if (!httpResponse.isCommitted()) {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again.");
            }
        }
    }
}
