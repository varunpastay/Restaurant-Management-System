package com.restro.controller.staff;

import com.restro.dao.StaffDao;
import com.restro.daoimpl.StaffDaoImpl;
import com.restro.dto.StaffDTO;
import com.restro.filters.StaffAuthFilter;
import com.restro.utility.AppLogger;
import com.restro.utility.PasswordUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shared login screen for both Kitchen and Counter staff - one form, one
 * staff table, the row's role decides which dashboard they land on. Kept
 * separate from AdminLoginServlet (Module 8) since admin and staff are
 * different tables/trust levels even though the mechanics look similar.
 */
@WebServlet(name = "StaffLoginServlet", urlPatterns = {"/staff/login"})
public class StaffLoginServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(StaffLoginServlet.class);
    private final StaffDao staffDao = new StaffDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/staff/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            showError(request, response, "Please enter both username and password.", redirect);
            return;
        }

        try {
            StaffDTO staff = staffDao.findByUsername(username.trim());
            if (staff == null || !staff.isActive()
                    || !PasswordUtil.matches(password, staff.getPasswordHash(), staff.getPasswordSalt())) {
                LOG.warn("Failed staff login attempt for username=" + username);
                showError(request, response, "Invalid username or password.", redirect);
                return;
            }

            // Invalidate any pre-login session and start fresh so a session id an
            // attacker fixed before authentication can't be reused post-login.
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(StaffAuthFilter.SESSION_STAFF_ID, staff.getStaffId());
            session.setAttribute(StaffAuthFilter.SESSION_STAFF_NAME, staff.getFullName());
            session.setAttribute(StaffAuthFilter.SESSION_STAFF_ROLE, staff.getRole().name());

            String landingPage = switch (staff.getRole()) {
                case KITCHEN -> "/kitchen/dashboard";
                case COUNTER -> "/counter/dashboard";
            };
            String target = (redirect != null && redirect.startsWith(landingPage.substring(0, landingPage.lastIndexOf('/'))))
                    ? redirect : landingPage;
            LOG.info("Staff login: " + staff.getUsername() + " (" + staff.getRole() + ")");
            response.sendRedirect(request.getContextPath() + target);
        } catch (SQLException e) {
            LOG.error("Staff login failed due to a database error", e);
            throw new ServletException("Login failed", e);
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message, String redirect)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("redirect", redirect);
        request.getRequestDispatcher("/WEB-INF/jsp/staff/login.jsp").forward(request, response);
    }
}
