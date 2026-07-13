package com.restro.controller.admin;

import com.restro.dao.RestaurantDao;
import com.restro.dao.StaffDao;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.StaffDaoImpl;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.StaffDTO;
import com.restro.dto.StaffRole;
import com.restro.utility.AppLogger;
import com.restro.utility.FlashUtil;
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

/** Staff account CRUD (kitchen/counter logins). A blank password on edit leaves the existing password unchanged. */
@WebServlet(name = "StaffServlet", urlPatterns = {"/admin/staff"})
public class StaffServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(StaffServlet.class);
    private final StaffDao staffDao = new StaffDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            request.setAttribute("staffList", staffDao.findAllByRestaurant(restaurant.getRestaurantId()));

            int editId = ValidationUtil.parseIntOrDefault(request.getParameter("edit"), -1);
            if (editId > 0) {
                request.setAttribute("editing", staffDao.findById(editId));
            }
            request.setAttribute("roles", StaffRole.values());
            request.setAttribute("error", FlashUtil.consumeError(session));
            request.setAttribute("success", FlashUtil.consumeSuccess(session));
            request.setAttribute("activeNav", "staff");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/staff.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load staff", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        try {
            switch (action == null ? "" : action) {
                case "save" -> handleSave(request, session);
                case "delete" -> handleDelete(request);
                default -> LOG.warn("Unknown staff action: " + action);
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            FlashUtil.setError(session, "That email is already taken.");
        } catch (SQLException e) {
            throw new ServletException("Failed to update staff", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/staff");
    }

    private void handleSave(HttpServletRequest request, HttpSession session) throws SQLException {
        int staffId = ValidationUtil.parseIntOrDefault(request.getParameter("staffId"), 0);
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String roleParam = request.getParameter("role");

        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(fullName) || ValidationUtil.isBlank(roleParam)) {
            FlashUtil.setError(session, "Email, full name, and role are all required.");
            return;
        }
        if (staffId == 0 && ValidationUtil.isBlank(password)) {
            FlashUtil.setError(session, "A password is required for a new staff account.");
            return;
        }

        StaffRole role = StaffRole.valueOf(roleParam);

        if (staffId > 0) {
            StaffDTO existing = staffDao.findById(staffId);
            if (existing == null) {
                FlashUtil.setError(session, "Staff member not found.");
                return;
            }
            existing.setEmail(email.trim());
            existing.setFullName(fullName.trim());
            existing.setPhone(request.getParameter("phone"));
            existing.setRole(role);
            existing.setActive(request.getParameter("active") != null);
            staffDao.update(existing);
            if (ValidationUtil.isNotBlank(password)) {
                String salt = PasswordUtil.generateSalt();
                staffDao.updatePassword(staffId, PasswordUtil.hash(password, salt), salt);
            }
            FlashUtil.setSuccess(session, "Staff member updated.");
        } else {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            String salt = PasswordUtil.generateSalt();
            StaffDTO staff = new StaffDTO();
            staff.setRestaurantId(restaurant.getRestaurantId());
            staff.setEmail(email.trim());
            staff.setFullName(fullName.trim());
            staff.setPhone(request.getParameter("phone"));
            staff.setRole(role);
            staff.setActive(true);
            staff.setPasswordSalt(salt);
            staff.setPasswordHash(PasswordUtil.hash(password, salt));
            staffDao.insert(staff);
            FlashUtil.setSuccess(session, "Staff member added.");
        }
    }

    private void handleDelete(HttpServletRequest request) throws SQLException {
        int staffId = ValidationUtil.parseIntOrDefault(request.getParameter("staffId"), -1);
        staffDao.delete(staffId);
    }
}
