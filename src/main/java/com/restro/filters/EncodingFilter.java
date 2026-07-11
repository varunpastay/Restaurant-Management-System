package com.restro.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * Forces a consistent character encoding on every request/response so
 * multi-language menu text, special instructions, and non-ASCII restaurant
 * names round-trip correctly. Must run before anything reads request
 * parameters, hence mapped first in web.xml.
 */
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) {
        String configured = filterConfig.getInitParameter("encoding");
        if (configured != null && !configured.isBlank()) {
            encoding = configured;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        chain.doFilter(request, response);
    }
}
