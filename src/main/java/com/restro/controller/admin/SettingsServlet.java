package com.restro.controller.admin;

import com.restro.dao.RestaurantDao;
import com.restro.dao.TaxDao;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.TaxDaoImpl;
import com.restro.dto.RestaurantDTO;
import com.restro.filters.AdminAuthFilter;
import com.restro.utility.AppLogger;
import com.restro.utility.FileUploadUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalTime;

/** Lets the restaurant owner edit every branding/business setting at runtime - no code change, no redeploy. */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/admin/settings"})
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 15L * 1024 * 1024)
public class SettingsServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(SettingsServlet.class);
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final TaxDao taxDao = new TaxDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            renderForm(request, response, restaurantDao.findFirst(), null);
        } catch (SQLException e) {
            throw new ServletException("Failed to load settings", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();

            String name = request.getParameter("name");
            if (ValidationUtil.isBlank(name)) {
                renderForm(request, response, restaurant, "Restaurant name is required.");
                return;
            }

            restaurant.setName(name.trim());
            restaurant.setAddress(ValidationUtil.sanitizeSingleLine(request.getParameter("address")));
            restaurant.setPhone(request.getParameter("phone"));
            restaurant.setEmail(request.getParameter("email"));
            restaurant.setGstin(request.getParameter("gstin"));
            restaurant.setCurrencyCode(request.getParameter("currencyCode"));
            restaurant.setCurrencySymbol(request.getParameter("currencySymbol"));
            restaurant.setThemeColor(request.getParameter("themeColor"));
            restaurant.setDarkModeDefault(request.getParameter("darkModeDefault") != null);
            restaurant.setOpen(request.getParameter("open") != null);

            BigDecimal serviceCharge = ValidationUtil.parseDecimalOrNull(request.getParameter("serviceChargePercent"));
            restaurant.setServiceChargePercent(serviceCharge != null ? serviceCharge : BigDecimal.ZERO);
            restaurant.setOpeningTime(parseTime(request.getParameter("openingTime")));
            restaurant.setClosingTime(parseTime(request.getParameter("closingTime")));

            String newLogoPath = FileUploadUtil.saveImage(request.getPart("logo"), "branding");
            if (newLogoPath != null) {
                FileUploadUtil.deleteIfExists(restaurant.getLogoPath());
                restaurant.setLogoPath(newLogoPath);
            }
            String newBannerPath = FileUploadUtil.saveImage(request.getPart("banner"), "branding");
            if (newBannerPath != null) {
                FileUploadUtil.deleteIfExists(restaurant.getBannerPath());
                restaurant.setBannerPath(newBannerPath);
            }

            restaurantDao.update(restaurant);
            LOG.info("Restaurant settings updated by adminId=" + request.getSession().getAttribute(AdminAuthFilter.SESSION_ADMIN_ID));

            request.setAttribute("success", "Settings saved successfully.");
            renderForm(request, response, restaurant, null);
        } catch (SQLException e) {
            throw new ServletException("Failed to save settings", e);
        } catch (IllegalArgumentException e) {
            try {
                renderForm(request, response, restaurantDao.findFirst(), e.getMessage());
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
        }
    }

    private LocalTime parseTime(String value) {
        return ValidationUtil.isBlank(value) ? null : LocalTime.parse(value);
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, RestaurantDTO restaurant, String error)
            throws ServletException, IOException {
        try {
            request.setAttribute("restaurant", restaurant);
            request.setAttribute("taxes", taxDao.findAllByRestaurant(restaurant.getRestaurantId()));
            request.setAttribute("error", error);
            request.setAttribute("activeNav", "settings");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/settings.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load tax settings", e);
        }
    }
}
