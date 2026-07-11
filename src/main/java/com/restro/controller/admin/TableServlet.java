package com.restro.controller.admin;

import com.restro.dao.QRCodeDao;
import com.restro.dao.RestaurantDao;
import com.restro.dao.RestaurantTableDao;
import com.restro.daoimpl.QRCodeDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.RestaurantTableDaoImpl;
import com.restro.dto.QRCodeDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.RestaurantTableDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.FlashUtil;
import com.restro.utility.TokenUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Table CRUD. Each table gets a random qr_token at creation time (used, not the numeric id, to resolve the customer-facing QR link). */
@WebServlet(name = "TableServlet", urlPatterns = {"/admin/tables"})
public class TableServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(TableServlet.class);
    private final RestaurantTableDao tableDao = new RestaurantTableDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final QRCodeDao qrCodeDao = new QRCodeDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            List<RestaurantTableDTO> tables = tableDao.findAllByRestaurant(restaurant.getRestaurantId());
            request.setAttribute("tables", tables);

            Map<Integer, QRCodeDTO> qrByTable = new HashMap<>();
            for (RestaurantTableDTO table : tables) {
                QRCodeDTO qr = qrCodeDao.findLatestByTable(table.getTableId());
                if (qr != null) {
                    qrByTable.put(table.getTableId(), qr);
                }
            }
            request.setAttribute("qrByTable", qrByTable);

            int editId = ValidationUtil.parseIntOrDefault(request.getParameter("edit"), -1);
            if (editId > 0) {
                request.setAttribute("editing", tableDao.findById(editId));
            }
            request.setAttribute("error", FlashUtil.consumeError(session));
            request.setAttribute("success", FlashUtil.consumeSuccess(session));
            request.setAttribute("activeNav", "tables");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/tables.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load tables", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        try {
            switch (action == null ? "" : action) {
                case "save" -> handleSave(request, session);
                case "delete" -> handleDelete(request, session);
                default -> LOG.warn("Unknown table action: " + action);
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            FlashUtil.setError(session, "This table can't be deleted because it has existing orders. Disable it instead.");
        } catch (SQLException e) {
            throw new ServletException("Failed to update tables", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/tables");
    }

    private void handleSave(HttpServletRequest request, HttpSession session) throws SQLException {
        int tableId = ValidationUtil.parseIntOrDefault(request.getParameter("tableId"), 0);
        String tableNo = request.getParameter("tableNo");
        int capacity = ValidationUtil.parseIntOrDefault(request.getParameter("capacity"), 4);

        if (ValidationUtil.isBlank(tableNo)) {
            FlashUtil.setError(session, "Table number/name is required.");
            return;
        }

        RestaurantDTO restaurant = restaurantDao.findFirst();
        if (tableDao.existsByTableNo(restaurant.getRestaurantId(), tableNo.trim(), tableId > 0 ? tableId : null)) {
            FlashUtil.setError(session, "Table \"" + tableNo.trim() + "\" already exists.");
            return;
        }

        if (tableId > 0) {
            RestaurantTableDTO existing = tableDao.findById(tableId);
            if (existing == null) {
                FlashUtil.setError(session, "Table not found.");
                return;
            }
            existing.setTableNo(tableNo.trim());
            existing.setCapacity(capacity);
            existing.setActive(request.getParameter("active") != null);
            tableDao.update(existing);
            FlashUtil.setSuccess(session, "Table updated.");
        } else {
            RestaurantTableDTO table = new RestaurantTableDTO();
            table.setRestaurantId(restaurant.getRestaurantId());
            table.setTableNo(tableNo.trim());
            table.setCapacity(capacity);
            table.setQrToken(TokenUtil.generateHexToken(16));
            table.setActive(true);
            tableDao.insert(table);
            FlashUtil.setSuccess(session, "Table added. Generate its QR code below.");
        }
    }

    private void handleDelete(HttpServletRequest request, HttpSession session) throws SQLException {
        int tableId = ValidationUtil.parseIntOrDefault(request.getParameter("tableId"), -1);
        tableDao.delete(tableId);
    }
}
