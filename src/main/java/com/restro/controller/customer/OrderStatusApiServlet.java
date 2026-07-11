package com.restro.controller.customer;

import com.restro.dao.OrderDao;
import com.restro.dao.OrderStatusHistoryDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.OrderStatusHistoryDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderStatusHistoryDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.JsonResponseUtil;
import com.restro.utility.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** JSON polling endpoint the order-tracking page calls every few seconds to refresh status without a full page reload. */
@WebServlet(name = "OrderStatusApiServlet", urlPatterns = {"/order/status"})
public class OrderStatusApiServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(OrderStatusApiServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final OrderStatusHistoryDao historyDao = new OrderStatusHistoryDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderNo = request.getParameter("orderNo");
        try {
            OrderDTO order = orderNo != null ? orderDao.findByOrderNo(orderNo) : null;
            if (order == null) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            List<OrderStatusHistoryDTO> history = historyDao.findByOrder(order.getOrderId());

            StringBuilder json = new StringBuilder();
            json.append("{\"orderNo\":").append(JsonUtil.quote(order.getOrderNo()))
                    .append(",\"status\":").append(JsonUtil.quote(order.getStatus().name()))
                    .append(",\"grandTotal\":").append(order.getGrandTotal())
                    .append(",\"history\":[");
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                OrderStatusHistoryDTO h = history.get(i);
                json.append("{\"status\":").append(JsonUtil.quote(h.getStatus().name()))
                        .append(",\"changedAt\":").append(JsonUtil.quote(String.valueOf(h.getChangedAt())))
                        .append("}");
            }
            json.append("]}");
            JsonResponseUtil.writeJson(response, json.toString());
        } catch (SQLException e) {
            LOG.error("Failed to load order status for " + orderNo, e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
