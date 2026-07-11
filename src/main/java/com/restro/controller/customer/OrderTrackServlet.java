package com.restro.controller.customer;

import com.restro.dao.OrderDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.utility.AppLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Renders the order-tracking page for a customer; live status updates come from OrderStatusApiServlet via AJAX polling. */
@WebServlet(name = "OrderTrackServlet", urlPatterns = {"/order/track"})
public class OrderTrackServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(OrderTrackServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderNo = request.getParameter("orderNo");
        try {
            OrderDTO order = orderNo != null ? orderDao.findByOrderNo(orderNo) : null;
            if (order == null) {
                request.setAttribute("reason", "We couldn't find that order.");
                request.getRequestDispatcher("/WEB-INF/jsp/customer/invalid-table.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            Object restaurantId = session.getAttribute(MenuServlet.SESSION_RESTAURANT_ID);
            RestaurantDTO restaurant = restaurantId != null
                    ? restaurantDao.findById((Integer) restaurantId)
                    : restaurantDao.findFirst();

            request.setAttribute("order", order);
            request.setAttribute("restaurant", restaurant);
            request.getRequestDispatcher("/WEB-INF/jsp/customer/order-track.jsp").forward(request, response);
        } catch (SQLException e) {
            LOG.error("Failed to load order " + orderNo, e);
            throw new ServletException("Failed to load order", e);
        }
    }
}
