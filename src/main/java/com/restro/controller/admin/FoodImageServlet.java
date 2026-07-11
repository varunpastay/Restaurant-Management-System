package com.restro.controller.admin;

import com.restro.dao.FoodImageDao;
import com.restro.daoimpl.FoodImageDaoImpl;
import com.restro.dto.FoodImageDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.FileUploadUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Per-image actions (delete one photo, make one the primary/menu-card photo) from the food item edit form. */
@WebServlet(name = "FoodImageServlet", urlPatterns = {"/admin/food/images"})
public class FoodImageServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(FoodImageServlet.class);
    private final FoodImageDao foodImageDao = new FoodImageDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        int foodImageId = ValidationUtil.parseIntOrDefault(request.getParameter("foodImageId"), -1);

        try {
            if ("delete".equals(action)) {
                FoodImageDTO image = foodImageDao.findByFoodItem(foodItemId).stream()
                        .filter(i -> i.getFoodImageId() == foodImageId).findFirst().orElse(null);
                if (image != null) {
                    foodImageDao.delete(foodImageId);
                    FileUploadUtil.deleteIfExists(image.getImagePath());
                }
            } else if ("setPrimary".equals(action)) {
                foodImageDao.setPrimary(foodItemId, foodImageId);
            } else {
                LOG.warn("Unknown food image action: " + action);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to update food images", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/food/form?id=" + foodItemId);
    }
}
