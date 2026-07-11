package com.restro.controller.admin;

import com.restro.dao.QRCodeDao;
import com.restro.dao.RestaurantTableDao;
import com.restro.daoimpl.QRCodeDaoImpl;
import com.restro.daoimpl.RestaurantTableDaoImpl;
import com.restro.dto.QRCodeDTO;
import com.restro.dto.RestaurantTableDTO;
import com.restro.utility.AppConfig;
import com.restro.utility.AppLogger;
import com.restro.utility.FlashUtil;
import com.restro.utility.QRCodeUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * (Re)generates a table's printable QR image. The encoded URL is
 * {@code {app.base.url}/menu?table={tableNo}&token={qrToken}} - table is
 * included only so the URL is human-readable; qr_token is what the customer
 * flow actually trusts to resolve the table (see RestaurantTableDao#findByToken).
 */
@WebServlet(name = "QRCodeGenerateServlet", urlPatterns = {"/admin/tables/qr/generate"})
public class QRCodeGenerateServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(QRCodeGenerateServlet.class);
    private final RestaurantTableDao tableDao = new RestaurantTableDaoImpl();
    private final QRCodeDao qrCodeDao = new QRCodeDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int tableId = ValidationUtil.parseIntOrDefault(request.getParameter("tableId"), -1);
        HttpSession session = request.getSession(true);
        try {
            RestaurantTableDTO table = tableDao.findById(tableId);
            if (table == null) {
                FlashUtil.setError(session, "Table not found.");
                response.sendRedirect(request.getContextPath() + "/admin/tables");
                return;
            }

            String baseUrl = AppConfig.get("app.base.url");
            String targetUrl = baseUrl + "/menu?table=" + java.net.URLEncoder.encode(table.getTableNo(), java.nio.charset.StandardCharsets.UTF_8)
                    + "&token=" + table.getQrToken();

            String imagePath = QRCodeUtil.generate(targetUrl);

            QRCodeDTO qrCode = new QRCodeDTO();
            qrCode.setTableId(tableId);
            qrCode.setImagePath(imagePath);
            qrCode.setTargetUrl(targetUrl);
            qrCodeDao.insert(qrCode);

            LOG.info("Generated QR for table " + table.getTableNo() + " -> " + targetUrl);
            FlashUtil.setSuccess(session, "QR code generated for table " + table.getTableNo() + ".");
        } catch (SQLException | IOException e) {
            throw new ServletException("Failed to generate QR code", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/tables");
    }
}
