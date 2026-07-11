package com.restro.controller.admin;

import com.restro.dao.CategoryDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.CategoryDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.CategoryDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.FileUploadUtil;
import com.restro.utility.FlashUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Category CRUD (unlimited food categories, each with a display order, optional image, and active/inactive toggle). */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/admin/categories"})
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 10L * 1024 * 1024)
public class CategoryServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(CategoryServlet.class);
    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            request.setAttribute("categories", categoryDao.findAllByRestaurant(restaurant.getRestaurantId()));

            int editId = ValidationUtil.parseIntOrDefault(request.getParameter("edit"), -1);
            if (editId > 0) {
                request.setAttribute("editing", categoryDao.findById(editId));
            }
            request.setAttribute("error", FlashUtil.consumeError(session));
            request.setAttribute("success", FlashUtil.consumeSuccess(session));
            request.setAttribute("activeNav", "categories");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/categories.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load categories", e);
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
                case "toggle" -> handleToggle(request);
                default -> LOG.warn("Unknown category action: " + action);
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            FlashUtil.setError(session, "This category still has food items assigned to it. "
                    + "Move or delete them first, or just disable the category instead.");
        } catch (SQLException e) {
            throw new ServletException("Failed to update categories", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }

    private void handleSave(HttpServletRequest request, HttpSession session)
            throws SQLException, IOException, ServletException {
        int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), 0);
        String name = request.getParameter("name");
        int displayOrder = ValidationUtil.parseIntOrDefault(request.getParameter("displayOrder"), 0);

        if (ValidationUtil.isBlank(name)) {
            FlashUtil.setError(session, "Category name is required.");
            return;
        }

        RestaurantDTO restaurant = restaurantDao.findFirst();
        if (categoryDao.existsByName(restaurant.getRestaurantId(), name.trim(), categoryId > 0 ? categoryId : null)) {
            FlashUtil.setError(session, "A category named \"" + name.trim() + "\" already exists.");
            return;
        }

        String imagePath;
        try {
            imagePath = FileUploadUtil.saveImage(request.getPart("image"), "categories");
        } catch (IllegalArgumentException e) {
            FlashUtil.setError(session, e.getMessage());
            return;
        }

        if (categoryId > 0) {
            CategoryDTO existing = categoryDao.findById(categoryId);
            if (existing == null) {
                FlashUtil.setError(session, "Category not found.");
                return;
            }
            existing.setName(name.trim());
            existing.setDisplayOrder(displayOrder);
            existing.setActive(request.getParameter("active") != null);
            if (imagePath != null) {
                FileUploadUtil.deleteIfExists(existing.getImagePath());
                existing.setImagePath(imagePath);
            }
            categoryDao.update(existing);
            FlashUtil.setSuccess(session, "Category updated.");
        } else {
            CategoryDTO category = new CategoryDTO();
            category.setRestaurantId(restaurant.getRestaurantId());
            category.setName(name.trim());
            category.setDisplayOrder(displayOrder);
            category.setActive(true);
            category.setImagePath(imagePath);
            categoryDao.insert(category);
            FlashUtil.setSuccess(session, "Category added.");
        }
    }

    private void handleDelete(HttpServletRequest request) throws SQLException {
        int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), -1);
        CategoryDTO existing = categoryDao.findById(categoryId);
        if (existing != null) {
            categoryDao.delete(categoryId);
            FileUploadUtil.deleteIfExists(existing.getImagePath());
        }
    }

    private void handleToggle(HttpServletRequest request) throws SQLException {
        int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), -1);
        CategoryDTO existing = categoryDao.findById(categoryId);
        if (existing != null) {
            existing.setActive(!existing.isActive());
            categoryDao.update(existing);
        }
    }
}
