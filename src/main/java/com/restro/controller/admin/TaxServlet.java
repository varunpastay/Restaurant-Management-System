package com.restro.controller.admin;

import com.restro.dao.RestaurantDao;
import com.restro.dao.TaxDao;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.TaxDaoImpl;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.TaxDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Manages the named, percentage-based tax lines shown on the Settings page
 * (CGST/SGST/GST/VAT/...). Kept as its own small servlet, separate from
 * SettingsServlet, since it's a CRUD list rather than a single-row form -
 * every action redirects back to /admin/settings so the tax table always
 * renders as part of that one page.
 */
@WebServlet(name = "TaxServlet", urlPatterns = {"/admin/settings/tax"})
public class TaxServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(TaxServlet.class);
    private final TaxDao taxDao = new TaxDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "add" -> handleAdd(request);
                case "toggle" -> handleToggle(request);
                case "delete" -> handleDelete(request);
                default -> LOG.warn("Unknown tax action: " + action);
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to update tax settings", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/settings");
    }

    private void handleAdd(HttpServletRequest request) throws SQLException {
        String name = request.getParameter("name");
        BigDecimal percent = ValidationUtil.parseDecimalOrNull(request.getParameter("percent"));
        if (ValidationUtil.isBlank(name) || percent == null || percent.signum() < 0) {
            return;
        }
        RestaurantDTO restaurant = restaurantDao.findFirst();
        TaxDTO tax = new TaxDTO();
        tax.setRestaurantId(restaurant.getRestaurantId());
        tax.setName(name.trim());
        tax.setPercent(percent);
        tax.setActive(true);
        taxDao.insert(tax);
        LOG.info("Tax added: " + name + " " + percent + "%");
    }

    private void handleToggle(HttpServletRequest request) throws SQLException {
        int taxId = ValidationUtil.parseIntOrDefault(request.getParameter("taxId"), -1);
        TaxDTO tax = taxDao.findById(taxId);
        if (tax != null) {
            tax.setActive(!tax.isActive());
            taxDao.update(tax);
        }
    }

    private void handleDelete(HttpServletRequest request) throws SQLException {
        int taxId = ValidationUtil.parseIntOrDefault(request.getParameter("taxId"), -1);
        taxDao.delete(taxId);
    }
}
