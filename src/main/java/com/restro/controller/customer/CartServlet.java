package com.restro.controller.customer;

import com.restro.dao.FoodItemDao;
import com.restro.daoimpl.FoodItemDaoImpl;
import com.restro.dto.CartDTO;
import com.restro.dto.CartItemDTO;
import com.restro.dto.FoodItemDTO;
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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * AJAX-only endpoint backing the menu page's cart drawer. GET returns the
 * current session cart as JSON (used to sync the UI on page load); POST
 * mutates it (add/updateQuantity/updateNote/remove/clear) and returns the
 * updated cart. Prices are always re-fetched server-side from FoodItemDao on
 * add - a client can never dictate what an item costs.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(CartServlet.class);
    static final String SESSION_CART = "cart";

    private final FoodItemDao foodItemDao = new FoodItemDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeCartJson(response, getOrCreateCart(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        Integer sessionTableId = (Integer) session.getAttribute(MenuServlet.SESSION_TABLE_ID);
        if (sessionTableId == null) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "No active table session. Please rescan the table QR code.");
            return;
        }

        CartDTO cart = getOrCreateCart(request);
        String action = request.getParameter("action");
        try {
            boolean handled = switch (action == null ? "" : action) {
                case "add" -> {
                    handleAdd(request, cart);
                    yield true;
                }
                case "updateQuantity" -> {
                    handleUpdateQuantity(request, cart);
                    yield true;
                }
                case "updateNote" -> {
                    handleUpdateNote(request, cart);
                    yield true;
                }
                case "remove" -> {
                    handleRemove(request, cart);
                    yield true;
                }
                case "clear" -> {
                    cart.clear();
                    yield true;
                }
                default -> false;
            };
            if (!handled) {
                JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown cart action: " + action);
                return;
            }
        } catch (IllegalArgumentException | SQLException e) {
            LOG.warn("Cart action failed: " + action + " - " + e.getMessage());
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        writeCartJson(response, cart);
    }

    private void handleAdd(HttpServletRequest request, CartDTO cart) throws SQLException {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        int quantity = Math.max(1, ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 1));
        String note = ValidationUtil.sanitizeSingleLine(request.getParameter("note"));

        FoodItemDTO food = foodItemDao.findById(foodItemId);
        if (food == null || !food.isAvailable()) {
            throw new IllegalArgumentException("That item is no longer available.");
        }
        cart.addOrIncrement(food, quantity, note);
    }

    private void handleUpdateQuantity(HttpServletRequest request, CartDTO cart) {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        int quantity = ValidationUtil.parseIntOrDefault(request.getParameter("quantity"), 0);
        cart.updateQuantity(foodItemId, quantity);
    }

    private void handleUpdateNote(HttpServletRequest request, CartDTO cart) {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        String note = ValidationUtil.sanitizeSingleLine(request.getParameter("note"));
        cart.updateInstructions(foodItemId, note);
    }

    private void handleRemove(HttpServletRequest request, CartDTO cart) {
        int foodItemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), -1);
        cart.removeItem(foodItemId);
    }

    /** Package-visible so OrderServlet can reach the same session cart without duplicating lookup logic. */
    static CartDTO getOrCreateCart(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        CartDTO cart = (CartDTO) session.getAttribute(SESSION_CART);
        Integer tableId = (Integer) session.getAttribute(MenuServlet.SESSION_TABLE_ID);
        if (cart == null || (tableId != null && !tableId.equals(cart.getTableId()))) {
            cart = new CartDTO();
            cart.setTableId(tableId);
            session.setAttribute(SESSION_CART, cart);
        }
        return cart;
    }

    private static void writeCartJson(HttpServletResponse response, CartDTO cart) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder();
        json.append("{\"items\":[");
        List<CartItemDTO> items = cart.getItems();
        for (int i = 0; i < items.size(); i++) {
            CartItemDTO item = items.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{")
                    .append("\"foodItemId\":").append(item.getFoodItemId()).append(",")
                    .append("\"name\":").append(JsonUtil.quote(item.getName())).append(",")
                    .append("\"unitPrice\":").append(item.getUnitPrice()).append(",")
                    .append("\"quantity\":").append(item.getQuantity()).append(",")
                    .append("\"specialInstructions\":").append(JsonUtil.quote(item.getSpecialInstructions())).append(",")
                    .append("\"lineTotal\":").append(item.getLineTotal()).append(",")
                    .append("\"imagePath\":").append(JsonUtil.quote(item.getImagePath()))
                    .append("}");
        }
        json.append("],")
                .append("\"subtotal\":").append(cart.getSubtotal()).append(",")
                .append("\"totalQuantity\":").append(cart.getTotalQuantity())
                .append("}");
        try (PrintWriter out = response.getWriter()) {
            out.write(json.toString());
        }
    }

}
