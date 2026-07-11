package com.restro.controller.admin;

import com.restro.dao.QRCodeDao;
import com.restro.dao.RestaurantDao;
import com.restro.dao.RestaurantTableDao;
import com.restro.daoimpl.QRCodeDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.RestaurantTableDaoImpl;
import com.restro.dto.QRCodeDTO;
import com.restro.dto.RestaurantTableDTO;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Clean, print-friendly single-table QR view opened in a new tab from the Tables screen - "Print QR Code". */
@WebServlet(name = "QRCodePrintServlet", urlPatterns = {"/admin/tables/qr/print"})
public class QRCodePrintServlet extends HttpServlet {

    private final RestaurantTableDao tableDao = new RestaurantTableDaoImpl();
    private final QRCodeDao qrCodeDao = new QRCodeDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int tableId = ValidationUtil.parseIntOrDefault(request.getParameter("tableId"), -1);
        try {
            RestaurantTableDTO table = tableDao.findById(tableId);
            QRCodeDTO qr = qrCodeDao.findLatestByTable(tableId);
            if (table == null || qr == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No QR code has been generated for this table yet.");
                return;
            }
            request.setAttribute("table", table);
            request.setAttribute("qr", qr);
            request.setAttribute("restaurant", restaurantDao.findFirst());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/qr-print.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load QR print view", e);
        }
    }
}
