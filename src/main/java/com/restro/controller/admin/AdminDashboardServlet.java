package com.restro.controller.admin;

import com.restro.dao.OrderDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.RestaurantDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Admin landing page: a quick operational snapshot (live order counts) plus navigation into every other admin section. */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            request.setAttribute("restaurant", restaurant);
            request.setAttribute("activeOrderCount", orderDao.findActiveForKitchen(restaurant.getRestaurantId()).size());
            request.setAttribute("awaitingBillingCount", orderDao.findAwaitingBilling(restaurant.getRestaurantId()).size());
            request.setAttribute("activeNav", "dashboard");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load admin dashboard", e);
        }
    }
}
