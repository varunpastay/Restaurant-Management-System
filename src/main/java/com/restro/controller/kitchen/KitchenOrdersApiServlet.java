package com.restro.controller.kitchen;

import com.restro.dao.OrderDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderItemDTO;
import com.restro.dto.RestaurantDTO;
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

/** JSON feed the kitchen dashboard polls to refresh its order queue without a full page reload. */
@WebServlet(name = "KitchenOrdersApiServlet", urlPatterns = {"/kitchen/orders"})
public class KitchenOrdersApiServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(KitchenOrdersApiServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            List<OrderDTO> orders = orderDao.findActiveForKitchen(restaurant.getRestaurantId());

            StringBuilder json = new StringBuilder("{\"orders\":[");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                appendOrder(json, orders.get(i));
            }
            json.append("]}");
            JsonResponseUtil.writeJson(response, json.toString());
        } catch (SQLException e) {
            LOG.error("Failed to load kitchen order queue", e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    private void appendOrder(StringBuilder json, OrderDTO order) {
        json.append("{")
                .append("\"orderId\":").append(order.getOrderId()).append(",")
                .append("\"orderNo\":").append(JsonUtil.quote(order.getOrderNo())).append(",")
                .append("\"tableNo\":").append(JsonUtil.quote(order.getTableNo())).append(",")
                .append("\"status\":").append(JsonUtil.quote(order.getStatus().name())).append(",")
                .append("\"customerNote\":").append(JsonUtil.quote(order.getCustomerNote())).append(",")
                .append("\"createdAt\":").append(JsonUtil.quote(JsonUtil.toIsoInstant(order.getCreatedAt()))).append(",")
                .append("\"items\":[");
        List<OrderItemDTO> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            OrderItemDTO item = items.get(i);
            json.append("{")
                    .append("\"name\":").append(JsonUtil.quote(item.getFoodNameSnapshot())).append(",")
                    .append("\"quantity\":").append(item.getQuantity()).append(",")
                    .append("\"specialInstructions\":").append(JsonUtil.quote(item.getSpecialInstructions()))
                    .append("}");
        }
        json.append("]}");
    }
}
