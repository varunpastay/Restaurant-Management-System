package com.restro.controller.counter;

import com.restro.dao.OrderDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderStatus;
import com.restro.filters.StaffAuthFilter;
import com.restro.utility.AppLogger;
import com.restro.utility.JsonResponseUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "CancelOrderServlet", urlPatterns = {"/counter/orders/cancel"})
public class CancelOrderServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(CancelOrderServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        HttpSession session = request.getSession(false);
        Integer staffId = session != null ? (Integer) session.getAttribute(StaffAuthFilter.SESSION_STAFF_ID) : null;

        try {
            OrderDTO order = orderDao.findById(orderId);
            if (order == null) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Order " + order.getOrderNo() + " can't be cancelled (already " + order.getStatus() + ")");
                return;
            }
            orderDao.cancelOrder(orderId, staffId);
            LOG.info("Order " + order.getOrderNo() + " cancelled by staffId=" + staffId);
            JsonResponseUtil.writeJson(response, "{\"orderId\":" + orderId + ",\"status\":\"CANCELLED\"}");
        } catch (SQLException e) {
            LOG.error("Failed to cancel order " + orderId, e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
