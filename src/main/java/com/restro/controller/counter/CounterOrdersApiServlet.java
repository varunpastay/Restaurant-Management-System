package com.restro.controller.counter;

import com.restro.dao.OrderDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.OrderDTO;
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

/** JSON feed the counter dashboard polls: every order that's been served but not yet paid. */
@WebServlet(name = "CounterOrdersApiServlet", urlPatterns = {"/counter/orders"})
public class CounterOrdersApiServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(CounterOrdersApiServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            List<OrderDTO> orders = orderDao.findAwaitingBilling(restaurant.getRestaurantId());

            StringBuilder json = new StringBuilder("{\"orders\":[");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                OrderDTO order = orders.get(i);
                json.append("{")
                        .append("\"orderId\":").append(order.getOrderId()).append(",")
                        .append("\"orderNo\":").append(JsonUtil.quote(order.getOrderNo())).append(",")
                        .append("\"tableNo\":").append(JsonUtil.quote(order.getTableNo())).append(",")
                        .append("\"itemCount\":").append(order.getItems().size()).append(",")
                        .append("\"grandTotal\":").append(order.getGrandTotal()).append(",")
                        .append("\"createdAt\":").append(JsonUtil.quote(String.valueOf(order.getCreatedAt())))
                        .append("}");
            }
            json.append("]}");
            JsonResponseUtil.writeJson(response, json.toString());
        } catch (SQLException e) {
            LOG.error("Failed to load billing queue", e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
