package com.restro.controller.admin;

import com.restro.dao.CategoryDao;
import com.restro.dao.FoodImageDao;
import com.restro.dao.FoodItemDao;
import com.restro.dao.RestaurantDao;
import com.restro.daoimpl.CategoryDaoImpl;
import com.restro.daoimpl.FoodImageDaoImpl;
import com.restro.daoimpl.FoodItemDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.dto.FoodImageDTO;
import com.restro.dto.FoodItemDTO;
import com.restro.dto.FoodType;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.SpiceLevel;
import com.restro.utility.AppLogger;
import com.restro.utility.FileUploadUtil;
import com.restro.utility.FlashUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;

/** Add/edit form for a single food item, including multi-image upload. Saves redirect back to the FoodItemServlet list. */
@WebServlet(name = "FoodItemFormServlet", urlPatterns = {"/admin/food/form"})
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 30L * 1024 * 1024)
public class FoodItemFormServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(FoodItemFormServlet.class);
    private final FoodItemDao foodItemDao = new FoodItemDaoImpl();
    private final FoodImageDao foodImageDao = new FoodImageDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            RestaurantDTO restaurant = restaurantDao.findFirst();
            request.setAttribute("categories", categoryDao.findAllByRestaurant(restaurant.getRestaurantId()));

            int itemId = ValidationUtil.parseIntOrDefault(request.getParameter("id"), -1);
            if (itemId > 0) {
                FoodItemDTO item = foodItemDao.findById(itemId);
                if (item == null) {
                    response.sendRedirect(request.getContextPath() + "/admin/food");
                    return;
                }
                request.setAttribute("item", item);
                request.setAttribute("images", foodImageDao.findByFoodItem(itemId));
            }
            request.setAttribute("foodTypes", FoodType.values());
            request.setAttribute("spiceLevels", SpiceLevel.values());
            request.setAttribute("error", FlashUtil.consumeError(session));
            request.setAttribute("activeNav", "food");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/food-item-form.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load food item form", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        int itemId = ValidationUtil.parseIntOrDefault(request.getParameter("foodItemId"), 0);
        try {
            String name = request.getParameter("name");
            BigDecimal price = ValidationUtil.parseDecimalOrNull(request.getParameter("price"));
            int categoryId = ValidationUtil.parseIntOrDefault(request.getParameter("categoryId"), -1);

            if (ValidationUtil.isBlank(name) || !ValidationUtil.isPositive(price) || categoryId <= 0) {
                FlashUtil.setError(session, "Name, a positive price, and a category are all required.");
                response.sendRedirect(request.getContextPath() + "/admin/food/form"
                        + (itemId > 0 ? "?id=" + itemId : ""));
                return;
            }

            RestaurantDTO restaurant = restaurantDao.findFirst();
            FoodItemDTO item = itemId > 0 ? foodItemDao.findById(itemId) : new FoodItemDTO();
            if (item == null) {
                response.sendRedirect(request.getContextPath() + "/admin/food");
                return;
            }

            item.setRestaurantId(restaurant.getRestaurantId());
            item.setCategoryId(categoryId);
            item.setName(name.trim());
            item.setDescription(request.getParameter("description"));
            item.setIngredients(request.getParameter("ingredients"));
            item.setPrice(price);
            item.setOfferPrice(ValidationUtil.parseDecimalOrNull(request.getParameter("offerPrice")));
            item.setPrepTimeMinutes(ValidationUtil.parseIntOrDefault(request.getParameter("prepTimeMinutes"), 15));
            item.setFoodType(FoodType.valueOf(request.getParameter("foodType")));
            item.setSpiceLevel(SpiceLevel.valueOf(request.getParameter("spiceLevel")));
            item.setAvailable(request.getParameter("available") != null);
            item.setRecommended(request.getParameter("recommended") != null);
            item.setBestseller(request.getParameter("bestseller") != null);
            item.setDisplayOrder(ValidationUtil.parseIntOrDefault(request.getParameter("displayOrder"), 0));

            boolean isNew = itemId <= 0;
            int savedItemId = isNew ? foodItemDao.insert(item) : itemId;
            if (!isNew) {
                foodItemDao.update(item);
            }

            saveUploadedImages(request, savedItemId);

            LOG.info((isNew ? "Food item added: " : "Food item updated: ") + item.getName());
            FlashUtil.setSuccess(session, isNew ? "Food item added." : "Food item updated.");
            response.sendRedirect(request.getContextPath() + "/admin/food/form?id=" + savedItemId);
        } catch (SQLException e) {
            throw new ServletException("Failed to save food item", e);
        }
    }

    private void saveUploadedImages(HttpServletRequest request, int foodItemId) throws IOException, ServletException, SQLException {
        Collection<Part> parts = request.getParts();
        boolean hasExistingImage = !foodImageDao.findByFoodItem(foodItemId).isEmpty();
        for (Part part : parts) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            String path = FileUploadUtil.saveImage(part, "food");
            if (path == null) {
                continue;
            }
            FoodImageDTO image = new FoodImageDTO();
            image.setFoodItemId(foodItemId);
            image.setImagePath(path);
            image.setPrimary(!hasExistingImage);
            foodImageDao.insert(image);
            hasExistingImage = true;
        }
    }
}
