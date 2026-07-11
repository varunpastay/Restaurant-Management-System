package com.restro.controller.admin;

import com.restro.dao.FoodImageDao;
import com.restro.dao.FoodItemDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.FoodImageDaoImpl;
import com.restro.daoimpl.FoodItemDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.FoodImageDTO;
import com.restro.dto.FoodItemDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.FileUploadUtil;
import com.restro.utility.FlashUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** The food item list/catalog screen: every item across every category, with quick availability toggle and delete. Add/edit happens on FoodItemFormServlet. */
@WebServlet(name = "FoodItemServlet", urlPatterns = {"/admin/food"})
public class FoodItemServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(FoodItemServlet.class);
    private final FoodItemDao foodItemDao = new FoodItemDaoImpl();
    private final FoodImageDao foodImageDao = new FoodImageDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            List<FoodItemDTO> items = foodItemDao.findAllByRestaurant(restaurant.getRestaurantId());
            request.setAttribute("items", items);
            request.setAttribute("error", FlashUtil.consumeError(session));
            request.setAttribute("success", FlashUtil.consumeSuccess(session));
            request.setAttribute("activeNav", "food");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/food-items.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load food items", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        try {
            switch (action == null ? "" : action) {
                case "delete" -> handleDelete(request);
                case "toggleAvailability" -> handleToggleAvailability(request);
                default -> LOG.warn("Unknown food item action: " + action);
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            FlashUtil.setError(session, "This item can't be deleted because it appears in past orders. "
                    + "Mark it unavailable instead to hide it from the menu.");
        } catch (SQLException e) {
            throw new ServletException("Failed to update food items", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/food");
    }

    private void handleDelete(HttpServletRequest request) throws SQLException {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        List<FoodImageDTO> images = foodImageDao.findByFoodItem(foodItemId);
        foodItemDao.delete(foodItemId);
        for (FoodImageDTO image : images) {
            FileUploadUtil.deleteIfExists(image.getImagePath());
        }
    }

    private void handleToggleAvailability(HttpServletRequest request) throws SQLException {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        FoodItemDTO item = foodItemDao.findById(foodItemId);
        if (item != null) {
            foodItemDao.setAvailability(foodItemId, !item.isAvailable());
        }
    }
}
