package com.restro.controller.kitchen;

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
import java.util.Map;

/**
 * Advances an order exactly one step through the kitchen-owned states
 * (Pending -&gt; Accepted -&gt; Preparing -&gt; Ready -&gt; Served). The
 * expected next status is looked up server-side from the order's current,
 * database-held status - never trusted from the client - so a manipulated
 * request can't skip steps (e.g. jump straight to Served). Cancelling and
 * completing (post-payment) belong to the Counter module, not here.
 */
@WebServlet(name = "KitchenStatusUpdateServlet", urlPatterns = {"/kitchen/orders/status"})
public class KitchenStatusUpdateServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(KitchenStatusUpdateServlet.class);

    private static final Map<OrderStatus, OrderStatus> NEXT_STATUS = Map.of(
            OrderStatus.PENDING, OrderStatus.ACCEPTED,
            OrderStatus.ACCEPTED, OrderStatus.PREPARING,
            OrderStatus.PREPARING, OrderStatus.READY,
            OrderStatus.READY, OrderStatus.SERVED
    );

    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        String requestedStatusParam = request.getParameter("status");
        HttpSession session = request.getSession(false);
        Integer staffId = session != null ? (Integer) session.getAttribute(StaffAuthFilter.SESSION_STAFF_ID) : null;

        try {
            OrderDTO order = orderDao.findById(orderId);
            if (order == null) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            OrderStatus requested;
            try {
                requested = OrderStatus.valueOf(requestedStatusParam);
            } catch (IllegalArgumentException | NullPointerException e) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown status");
                return;
            }

            OrderStatus expectedNext = NEXT_STATUS.get(order.getStatus());
            if (expectedNext == null || expectedNext != requested) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Order " + order.getOrderNo() + " is currently " + order.getStatus()
                                + " - it can't move to " + requestedStatusParam + " from here.");
                return;
            }

            orderDao.updateStatus(orderId, requested, staffId);
            LOG.info("Order " + order.getOrderNo() + " -> " + requested + " (staffId=" + staffId + ")");
            JsonResponseUtil.writeJson(response, "{\"orderId\":" + orderId + ",\"status\":\"" + requested + "\"}");
        } catch (SQLException e) {
            LOG.error("Failed to update order status for orderId=" + orderId, e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
