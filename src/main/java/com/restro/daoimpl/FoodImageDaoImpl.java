package com.restro.daoimpl;

import com.restro.dao.FoodImageDao;
import com.restro.dto.FoodImageDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodImageDaoImpl implements FoodImageDao {

    private static final String SELECT_BASE =
            "SELECT food_image_id, food_item_id, image_path, is_primary, display_order, created_at FROM food_image ";

    @Override
    public List<FoodImageDTO> findByFoodItem(int foodItemId) throws SQLException {
        String sql = SELECT_BASE + "WHERE food_item_id = ? ORDER BY is_primary DESC, display_order";
        List<FoodImageDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodImageDTO img = new FoodImageDTO();
                    img.setFoodImageId(rs.getInt("food_image_id"));
                    img.setFoodItemId(rs.getInt("food_item_id"));
                    img.setImagePath(rs.getString("image_path"));
                    img.setPrimary(rs.getBoolean("is_primary"));
                    img.setDisplayOrder(rs.getInt("display_order"));
                    img.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(img);
                }
            }
        }
        return result;
    }

    @Override
    public FoodImageDTO findPrimaryByFoodItem(int foodItemId) throws SQLException {
        String sql = SELECT_BASE + "WHERE food_item_id = ? ORDER BY is_primary DESC, display_order LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                FoodImageDTO img = new FoodImageDTO();
                img.setFoodImageId(rs.getInt("food_image_id"));
                img.setFoodItemId(rs.getInt("food_item_id"));
                img.setImagePath(rs.getString("image_path"));
                img.setPrimary(rs.getBoolean("is_primary"));
                img.setDisplayOrder(rs.getInt("display_order"));
                img.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return img;
            }
        }
    }

    @Override
    public int insert(FoodImageDTO image) throws SQLException {
        String sql = "INSERT INTO food_image (food_item_id, image_path, is_primary, display_order) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, image.getFoodItemId());
            ps.setString(2, image.getImagePath());
            ps.setBoolean(3, image.isPrimary());
            ps.setInt(4, image.getDisplayOrder());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean delete(int foodImageId) throws SQLException {
        String sql = "DELETE FROM food_image WHERE food_image_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodImageId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean setPrimary(int foodItemId, int foodImageId) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement clear = conn.prepareStatement(
                        "UPDATE food_image SET is_primary = 0 WHERE food_item_id = ?")) {
                    clear.setInt(1, foodItemId);
                    clear.executeUpdate();
                }
                boolean updated;
                try (PreparedStatement set = conn.prepareStatement(
                        "UPDATE food_image SET is_primary = 1 WHERE food_image_id = ? AND food_item_id = ?")) {
                    set.setInt(1, foodImageId);
                    set.setInt(2, foodItemId);
                    updated = set.executeUpdate() > 0;
                }
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

}
