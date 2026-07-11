package com.restro.controller.admin;

import com.restro.dao.QRCodeDao;
import com.restro.dao.RestaurantTableDao;
import com.restro.dao.UploadedFileDao;
import com.restro.daoimpl.QRCodeDaoImpl;
import com.restro.daoimpl.RestaurantTableDaoImpl;
import com.restro.daoimpl.UploadedFileDaoImpl;
import com.restro.dto.QRCodeDTO;
import com.restro.dto.RestaurantTableDTO;
import com.restro.dto.UploadedFileDTO;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Downloads a table's most recently generated QR PNG as an attachment. */
@WebServlet(name = "QRCodeDownloadServlet", urlPatterns = {"/admin/tables/qr/download"})
public class QRCodeDownloadServlet extends HttpServlet {

    private final RestaurantTableDao tableDao = new RestaurantTableDaoImpl();
    private final QRCodeDao qrCodeDao = new QRCodeDaoImpl();
    private final UploadedFileDao uploadedFileDao = new UploadedFileDaoImpl();

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

            UploadedFileDTO file = uploadedFileDao.findByPath(qr.getImagePath());
            if (file == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "QR image file is missing.");
                return;
            }

            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "attachment; filename=\"table-" + table.getTableNo() + "-qr.png\"");
            response.getOutputStream().write(file.getData());
        } catch (SQLException e) {
            throw new ServletException("Failed to download QR code", e);
        }
    }
}
