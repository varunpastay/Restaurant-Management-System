package com.restro.controller.customer;

import com.restro.dao.CategoryDao;
import com.restro.dao.FoodItemDao;
import com.restro.dao.RestaurantDao;
import com.restro.dao.RestaurantTableDao;
import com.restro.daoimpl.CategoryDaoImpl;
import com.restro.daoimpl.FoodItemDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.RestaurantTableDaoImpl;
import com.restro.dto.CategoryDTO;
import com.restro.dto.FoodItemDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.RestaurantTableDTO;
import com.restro.utility.AppLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the no-login customer flow. A table's printed QR code
 * points here with ?token=&lt;qrToken&gt; (an optional ?table= is only for
 * human-readable URLs; the token is what's actually trusted to resolve the
 * table - see RestaurantTableDao#findByToken).
 */
@WebServlet(name = "MenuServlet", urlPatterns = {"/menu"})
public class MenuServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(MenuServlet.class);

    public static final String SESSION_TABLE_ID = "tableId";
    public static final String SESSION_TABLE_NO = "tableNo";
    public static final String SESSION_TABLE_TOKEN = "tableToken";
    public static final String SESSION_RESTAURANT_ID = "restaurantId";

    private final RestaurantTableDao tableDao = new RestaurantTableDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final FoodItemDao foodItemDao = new FoodItemDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token == null || token.isBlank()) {
            showInvalid(request, response, "This QR code is missing its table token.");
            return;
        }

        try {
            RestaurantTableDTO table = tableDao.findByToken(token);
            if (table == null) {
                showInvalid(request, response, "We couldn't find that table. Please ask a staff member for help.");
                return;
            }

            RestaurantDTO restaurant = restaurantDao.findById(table.getRestaurantId());

            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_TABLE_ID, table.getTableId());
            session.setAttribute(SESSION_TABLE_NO, table.getTableNo());
            session.setAttribute(SESSION_TABLE_TOKEN, table.getQrToken());
            session.setAttribute(SESSION_RESTAURANT_ID, restaurant.getRestaurantId());

            List<CategoryDTO> categories = categoryDao.findActiveByRestaurant(restaurant.getRestaurantId());
            List<FoodItemDTO> foodItems = foodItemDao.findAvailableByRestaurant(restaurant.getRestaurantId());

            Map<CategoryDTO, List<FoodItemDTO>> menu = new LinkedHashMap<>();
            for (CategoryDTO category : categories) {
                List<FoodItemDTO> itemsInCategory = foodItems.stream()
                        .filter(f -> f.getCategoryId() == category.getCategoryId())
                        .toList();
                if (!itemsInCategory.isEmpty()) {
                    menu.put(category, itemsInCategory);
                }
            }

            Object lastOrderNo = session.getAttribute(OrderServlet.SESSION_LAST_ORDER_NO);

            request.setAttribute("restaurant", restaurant);
            request.setAttribute("table", table);
            request.setAttribute("menu", menu);
            request.setAttribute("lastOrderNo", lastOrderNo);
            request.getRequestDispatcher("/WEB-INF/jsp/customer/menu.jsp").forward(request, response);
        } catch (SQLException e) {
            LOG.error("Failed to load menu for token=" + token, e);
            throw new ServletException("Failed to load menu", e);
        }
    }

    private void showInvalid(HttpServletRequest request, HttpServletResponse response, String reason)
            throws ServletException, IOException {
        request.setAttribute("reason", reason);
        request.getRequestDispatcher("/WEB-INF/jsp/customer/invalid-table.jsp").forward(request, response);
    }
}
