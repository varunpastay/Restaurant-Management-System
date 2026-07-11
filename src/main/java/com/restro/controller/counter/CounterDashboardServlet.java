package com.restro.controller.counter;

import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.RestaurantDTO;
import com.restro.filters.StaffAuthFilter;
import com.restro.utility.AppConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Renders the counter dashboard shell; the billing queue is fetched and refreshed via AJAX (CounterOrdersApiServlet). */
@WebServlet(name = "CounterDashboardServlet", urlPatterns = {"/counter/dashboard"})
public class CounterDashboardServlet extends HttpServlet {

    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            request.setAttribute("restaurant", restaurant);
            request.setAttribute("staffName", request.getSession().getAttribute(StaffAuthFilter.SESSION_STAFF_NAME));
            request.setAttribute("refreshIntervalSeconds",
                    AppConfig.getInt("dashboard.refresh.interval.seconds", 5));
            request.getRequestDispatcher("/WEB-INF/jsp/counter/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load counter dashboard", e);
        }
    }
}
