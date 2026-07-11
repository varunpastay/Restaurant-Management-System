package com.restro.controller.admin;

import com.lowagie.text.DocumentException;
import com.restro.dao.ReportDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.ReportDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.RestaurantDTO;
import com.restro.utility.PdfReportUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;

@WebServlet(name = "ReportPdfExportServlet", urlPatterns = {"/admin/reports/export/pdf"})
public class ReportPdfExportServlet extends HttpServlet {

    private final ReportDao reportDao = new ReportDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String period = request.getParameter("period");
        if (period == null || period.isBlank()) {
            period = "today";
        }
        LocalDateTime from = ReportServlet.resolveFrom(period);
        LocalDateTime to = LocalDateTime.now();

        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            int restaurantId = restaurant.getRestaurantId();
            BigDecimal totalRevenue = reportDao.getTotalRevenue(restaurantId, from, to);
            int orderCount = reportDao.getOrderCount(restaurantId, from, to);
            BigDecimal avgOrderValue = orderCount > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"sales-report-" + period + ".pdf\"");
            PdfReportUtil.generate(response.getOutputStream(), restaurant, period, totalRevenue, orderCount, avgOrderValue,
                    reportDao.getTopSellingFoods(restaurantId, from, to, 10),
                    reportDao.getLeastSellingFoods(restaurantId, from, to, 10),
                    reportDao.getDailySales(restaurantId, from, to));
        } catch (SQLException | DocumentException e) {
            throw new ServletException("Failed to export PDF report", e);
        }
    }
}
