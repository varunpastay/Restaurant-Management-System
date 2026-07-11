package com.restro.controller.customer;

import com.restro.dao.DiscountDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.DiscountDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.CartDTO;
import com.restro.dto.DiscountDTO;
import com.restro.dto.OrderDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.service.OrderPlacementService;
import com.restro.utility.AppLogger;
import com.restro.utility.JsonResponseUtil;
import com.restro.utility.JsonUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Converts the session cart into a real, kitchen-visible order. AJAX-only: called by the "Place Order" button in the cart drawer. */
@WebServlet(name = "OrderServlet", urlPatterns = {"/order/place"})
public class OrderServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(OrderServlet.class);
    public static final String SESSION_LAST_ORDER_NO = "lastOrderNo";

    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final DiscountDao discountDao = new DiscountDaoImpl();
    private final OrderPlacementService orderPlacementService = new OrderPlacementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        Integer restaurantId = (Integer) session.getAttribute(MenuServlet.SESSION_RESTAURANT_ID);
        Integer tableId = (Integer) session.getAttribute(MenuServlet.SESSION_TABLE_ID);
        if (restaurantId == null || tableId == null) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "No active table session. Please rescan the table QR code.");
            return;
        }

        CartDTO cart = CartServlet.getOrCreateCart(request);
        if (cart.isEmpty()) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Your cart is empty.");
            return;
        }

        String customerNote = ValidationUtil.sanitizeSingleLine(request.getParameter("customerNote"));
        String discountCode = request.getParameter("discountCode");

        try {
            RestaurantDTO restaurant = restaurantDao.findById(restaurantId);
            if (!restaurant.isOpen()) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Sorry, the restaurant is currently closed and not accepting orders.");
                return;
            }

            DiscountDTO discount = null;
            if (ValidationUtil.isNotBlank(discountCode)) {
                discount = discountDao.findActiveByCode(restaurantId, discountCode.trim());
                if (discount == null) {
                    JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "That discount code isn't valid.");
                    return;
                }
            }

            OrderDTO order = orderPlacementService.placeOrder(cart, restaurant, discount, customerNote);
            cart.clear();
            session.setAttribute(SESSION_LAST_ORDER_NO, order.getOrderNo());

            JsonResponseUtil.writeJson(response, "{\"orderNo\":" + JsonUtil.quote(order.getOrderNo())
                    + ",\"orderId\":" + order.getOrderId() + "}");
            LOG.info("Order placed: " + order.getOrderNo());
        } catch (SQLException e) {
            LOG.error("Failed to place order", e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not place your order. Please try again.");
        } catch (IllegalArgumentException e) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
