package com.restro.controller.admin;

import com.restro.dao.ReportDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.ReportDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.DailySalesDTO;
import com.restro.dto.FoodSalesDTO;
import com.restro.dto.HourlySalesDTO;
import com.restro.dto.RestaurantDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Sales dashboard: today/week/month/year revenue, top/least sellers, peak hours, average order value, and a daily revenue trend. */
@WebServlet(name = "ReportServlet", urlPatterns = {"/admin/reports"})
public class ReportServlet extends HttpServlet {

    private final ReportDao reportDao = new ReportDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String period = request.getParameter("period");
        if (period == null || period.isBlank()) {
            period = "today";
        }
        LocalDateTime from = resolveFrom(period);
        LocalDateTime to = LocalDateTime.now();

        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            int restaurantId = restaurant.getRestaurantId();

            BigDecimal totalRevenue = reportDao.getTotalRevenue(restaurantId, from, to);
            int orderCount = reportDao.getOrderCount(restaurantId, from, to);
            BigDecimal avgOrderValue = orderCount > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            List<HourlySalesDTO> hourlySales = reportDao.getHourlySales(restaurantId, from, to);
            BigDecimal maxHourlyRevenue = hourlySales.stream().map(HourlySalesDTO::getRevenue)
                    .max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);

            List<DailySalesDTO> dailySales = reportDao.getDailySales(restaurantId, from, to);
            BigDecimal maxDailyRevenue = dailySales.stream().map(DailySalesDTO::getRevenue)
                    .max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);

            List<FoodSalesDTO> topSelling = reportDao.getTopSellingFoods(restaurantId, from, to, 5);
            BigDecimal maxTopSellingQty = topSelling.stream().map(f -> BigDecimal.valueOf(f.getTotalQuantity()))
                    .max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);

            request.setAttribute("restaurant", restaurant);
            request.setAttribute("period", period);
            request.setAttribute("fromDate", from.toLocalDate());
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("orderCount", orderCount);
            request.setAttribute("avgOrderValue", avgOrderValue);
            request.setAttribute("topSelling", topSelling);
            request.setAttribute("maxTopSellingQty", maxTopSellingQty);
            request.setAttribute("leastSelling", reportDao.getLeastSellingFoods(restaurantId, from, to, 5));
            request.setAttribute("hourlySales", hourlySales);
            request.setAttribute("maxHourlyRevenue", maxHourlyRevenue);
            request.setAttribute("dailySales", dailySales);
            request.setAttribute("maxDailyRevenue", maxDailyRevenue);
            request.setAttribute("activeNav", "reports");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/reports.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load sales report", e);
        }
    }

    static LocalDateTime resolveFrom(String period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "week" -> today.with(DayOfWeek.MONDAY).atStartOfDay();
            case "month" -> today.withDayOfMonth(1).atStartOfDay();
            case "year" -> today.withDayOfYear(1).atStartOfDay();
            default -> today.atStartOfDay();
        };
    }
}
