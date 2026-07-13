package com.restro.controller.admin;

import com.restro.dao.AdminDao;
import com.restro.daoimpl.AdminDaoImpl;
import com.restro.dto.AdminDTO;
import com.restro.filters.AdminAuthFilter;
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
import java.time.LocalDateTime;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/admin/login"})
public class AdminLoginServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(AdminLoginServlet.class);
    private final AdminDao adminDao = new AdminDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute(AdminAuthFilter.SESSION_ADMIN_ID) != null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/jsp/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            showError(request, response, "Please enter both email and password.");
            return;
        }

        try {
            AdminDTO admin = adminDao.findByEmail(email.trim());
            if (admin == null || !admin.isActive()
                    || !PasswordUtil.matches(password, admin.getPasswordHash(), admin.getPasswordSalt())) {
                LOG.warn("Failed admin login attempt for email=" + email);
                showError(request, response, "Invalid email or password.");
                return;
            }

            // Invalidate any pre-login session and start fresh so a session id an
            // attacker fixed before authentication can't be reused post-login.
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(AdminAuthFilter.SESSION_ADMIN_ID, admin.getAdminId());
            session.setAttribute(AdminAuthFilter.SESSION_ADMIN_NAME, admin.getFullName());

            adminDao.updateLastLogin(admin.getAdminId(), LocalDateTime.now());
            LOG.info("Admin login: " + admin.getEmail());
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            LOG.error("Admin login failed due to a database error", e);
            throw new ServletException("Login failed", e);
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/jsp/admin/login.jsp").forward(request, response);
    }
}
