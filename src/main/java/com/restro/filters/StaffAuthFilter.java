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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Guards /kitchen/* and /counter/* - requires a logged-in staff member whose
 * role matches the section being accessed (a kitchen login can't reach
 * counter screens and vice versa, and neither can an unauthenticated
 * request). Unauthenticated/wrong-role requests are redirected to the shared
 * staff login page rather than shown an error, since the natural next step
 * for staff is simply to log in.
 */
public class StaffAuthFilter implements Filter {

    public static final String SESSION_STAFF_ID = "staffId";
    public static final String SESSION_STAFF_NAME = "staffFullName";
    public static final String SESSION_STAFF_ROLE = "staffRole";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String requiredRole = path.startsWith("/kitchen/") ? "KITCHEN" : path.startsWith("/counter/") ? "COUNTER" : null;

        Object staffId = session != null ? session.getAttribute(SESSION_STAFF_ID) : null;
        Object staffRole = session != null ? session.getAttribute(SESSION_STAFF_ROLE) : null;

        if (staffId == null || requiredRole == null || !requiredRole.equals(staffRole)) {
            String redirectTarget = URLEncoder.encode(path, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/staff/login?redirect=" + redirectTarget);
            return;
        }
        chain.doFilter(request, response);
    }
}
