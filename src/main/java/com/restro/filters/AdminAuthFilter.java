package com.restro.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Guards every /admin/* page except the login screen itself - requires a logged-in admin session. */
public class AdminAuthFilter implements Filter {

    public static final String SESSION_ADMIN_ID = "adminId";
    public static final String SESSION_ADMIN_NAME = "adminFullName";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.equals("/admin/login")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_ADMIN_ID) == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }
        chain.doFilter(request, response);
    }
}
