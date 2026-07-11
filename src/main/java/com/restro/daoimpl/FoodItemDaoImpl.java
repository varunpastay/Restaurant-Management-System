package com.restro.daoimpl;

import com.restro.dao.FoodItemDao;
import com.restro.dto.FoodItemDTO;
import com.restro.dto.FoodType;
import com.restro.dto.SpiceLevel;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDaoImpl implements FoodItemDao {

    // Joins category for its name and food_image for the primary photo, so a single query gives
    // menu/admin screens everything they need without N+1 lookups per item.
    private static final String SELECT_BASE =
            "SELECT fi.food_item_id, fi.restaurant_id, fi.category_id, fi.name, fi.description, fi.ingredients, " +
            "fi.price, fi.offer_price, fi.prep_time_minutes, fi.food_type, fi.spice_level, fi.is_available, " +
            "fi.is_recommended, fi.is_bestseller, fi.display_order, fi.created_at, fi.updated_at, " +
            "c.name AS category_name, " +
            "(SELECT img.image_path FROM food_image img WHERE img.food_item_id = fi.food_item_id " +
            "   ORDER BY img.is_primary DESC, img.display_order LIMIT 1) AS primary_image_path " +
            "FROM food_item fi JOIN category c ON c.category_id = fi.category_id ";

    @Override
    public List<FoodItemDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE fi.restaurant_id = ? ORDER BY c.display_order, fi.display_order, fi.name";
        return queryList(sql, restaurantId);
    }

    @Override
    public List<FoodItemDTO> findAvailableByCategory(int categoryId) throws SQLException {
        String sql = SELECT_BASE + "WHERE fi.category_id = ? AND fi.is_available = 1 " +
                "ORDER BY fi.display_order, fi.name";
        List<FoodItemDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItemDTO f = new FoodItemDTO();
                    f.setFoodItemId(rs.getInt("food_item_id"));
                    f.setRestaurantId(rs.getInt("restaurant_id"));
                    f.setCategoryId(rs.getInt("category_id"));
                    f.setName(rs.getString("name"));
                    f.setDescription(rs.getString("description"));
                    f.setIngredients(rs.getString("ingredients"));
                    f.setPrice(rs.getBigDecimal("price"));
                    f.setOfferPrice(rs.getBigDecimal("offer_price"));
                    f.setPrepTimeMinutes(rs.getInt("prep_time_minutes"));
                    f.setFoodType(FoodType.valueOf(rs.getString("food_type")));
                    f.setSpiceLevel(SpiceLevel.valueOf(rs.getString("spice_level")));
                    f.setAvailable(rs.getBoolean("is_available"));
                    f.setRecommended(rs.getBoolean("is_recommended"));
                    f.setBestseller(rs.getBoolean("is_bestseller"));
                    f.setDisplayOrder(rs.getInt("display_order"));
                    f.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    f.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                    f.setCategoryName(rs.getString("category_name"));
                    f.setPrimaryImagePath(rs.getString("primary_image_path"));
                    result.add(f);
                }
            }
        }
        return result;
    }

    @Override
    public List<FoodItemDTO> findAvailableByRestaurant(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE fi.restaurant_id = ? AND fi.is_available = 1 " +
                "ORDER BY c.display_order, fi.display_order, fi.name";
        return queryList(sql, restaurantId);
    }

    private List<FoodItemDTO> queryList(String sql, int restaurantId) throws SQLException {
        List<FoodItemDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItemDTO f = new FoodItemDTO();
                    f.setFoodItemId(rs.getInt("food_item_id"));
                    f.setRestaurantId(rs.getInt("restaurant_id"));
                    f.setCategoryId(rs.getInt("category_id"));
                    f.setName(rs.getString("name"));
                    f.setDescription(rs.getString("description"));
                    f.setIngredients(rs.getString("ingredients"));
                    f.setPrice(rs.getBigDecimal("price"));
                    f.setOfferPrice(rs.getBigDecimal("offer_price"));
                    f.setPrepTimeMinutes(rs.getInt("prep_time_minutes"));
                    f.setFoodType(FoodType.valueOf(rs.getString("food_type")));
                    f.setSpiceLevel(SpiceLevel.valueOf(rs.getString("spice_level")));
                    f.setAvailable(rs.getBoolean("is_available"));
                    f.setRecommended(rs.getBoolean("is_recommended"));
                    f.setBestseller(rs.getBoolean("is_bestseller"));
                    f.setDisplayOrder(rs.getInt("display_order"));
                    f.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    f.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                    f.setCategoryName(rs.getString("category_name"));
                    f.setPrimaryImagePath(rs.getString("primary_image_path"));
                    result.add(f);
                }
            }
        }
        return result;
    }

    @Override
    public FoodItemDTO findById(int foodItemId) throws SQLException {
        String sql = SELECT_BASE + "WHERE fi.food_item_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                FoodItemDTO f = new FoodItemDTO();
                f.setFoodItemId(rs.getInt("food_item_id"));
                f.setRestaurantId(rs.getInt("restaurant_id"));
                f.setCategoryId(rs.getInt("category_id"));
                f.setName(rs.getString("name"));
                f.setDescription(rs.getString("description"));
                f.setIngredients(rs.getString("ingredients"));
                f.setPrice(rs.getBigDecimal("price"));
                f.setOfferPrice(rs.getBigDecimal("offer_price"));
                f.setPrepTimeMinutes(rs.getInt("prep_time_minutes"));
                f.setFoodType(FoodType.valueOf(rs.getString("food_type")));
                f.setSpiceLevel(SpiceLevel.valueOf(rs.getString("spice_level")));
                f.setAvailable(rs.getBoolean("is_available"));
                f.setRecommended(rs.getBoolean("is_recommended"));
                f.setBestseller(rs.getBoolean("is_bestseller"));
                f.setDisplayOrder(rs.getInt("display_order"));
                f.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                f.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                f.setCategoryName(rs.getString("category_name"));
                f.setPrimaryImagePath(rs.getString("primary_image_path"));
                return f;
            }
        }
    }

    @Override
    public int insert(FoodItemDTO f) throws SQLException {
        String sql = "INSERT INTO food_item (restaurant_id, category_id, name, description, ingredients, " +
                "price, offer_price, prep_time_minutes, food_type, spice_level, is_available, is_recommended, " +
                "is_bestseller, display_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWriteParams(ps, f);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(FoodItemDTO f) throws SQLException {
        String sql = "UPDATE food_item SET category_id = ?, name = ?, description = ?, ingredients = ?, " +
                "price = ?, offer_price = ?, prep_time_minutes = ?, food_type = ?, spice_level = ?, " +
                "is_available = ?, is_recommended = ?, is_bestseller = ?, display_order = ? WHERE food_item_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int nextIndex = bindWriteParams(ps, f);
            ps.setInt(nextIndex, f.getFoodItemId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Binds the 14 shared insert/update columns in order, returning the next free parameter index. */
    private int bindWriteParams(PreparedStatement ps, FoodItemDTO f) throws SQLException {
        int i = 1;
        ps.setInt(i++, f.getRestaurantId());
        ps.setInt(i++, f.getCategoryId());
        ps.setString(i++, f.getName());
        ps.setString(i++, f.getDescription());
        ps.setString(i++, f.getIngredients());
        ps.setBigDecimal(i++, f.getPrice());
        ps.setBigDecimal(i++, f.getOfferPrice());
        ps.setInt(i++, f.getPrepTimeMinutes());
        ps.setString(i++, f.getFoodType().name());
        ps.setString(i++, f.getSpiceLevel().name());
        ps.setBoolean(i++, f.isAvailable());
        ps.setBoolean(i++, f.isRecommended());
        ps.setBoolean(i++, f.isBestseller());
        ps.setInt(i++, f.getDisplayOrder());
        return i;
    }

    @Override
    public boolean delete(int foodItemId) throws SQLException {
        String sql = "DELETE FROM food_item WHERE food_item_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodItemId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean setAvailability(int foodItemId, boolean available) throws SQLException {
        String sql = "UPDATE food_item SET is_available = ? WHERE food_item_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, foodItemId);
            return ps.executeUpdate() > 0;
        }
    }

}
