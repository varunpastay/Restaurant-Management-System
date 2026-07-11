package com.restro.daoimpl;

import com.restro.dao.CategoryDao;
import com.restro.dto.CategoryDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDaoImpl implements CategoryDao {

    private static final String SELECT_BASE =
            "SELECT category_id, restaurant_id, name, display_order, image_path, is_active, created_at, updated_at " +
            "FROM category ";

    @Override
    public List<CategoryDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        return queryList(SELECT_BASE + "WHERE restaurant_id = ? ORDER BY display_order, name", restaurantId);
    }

    @Override
    public List<CategoryDTO> findActiveByRestaurant(int restaurantId) throws SQLException {
        return queryList(SELECT_BASE + "WHERE restaurant_id = ? AND is_active = 1 ORDER BY display_order, name",
                restaurantId);
    }

    private List<CategoryDTO> queryList(String sql, int restaurantId) throws SQLException {
        List<CategoryDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoryDTO c = new CategoryDTO();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setRestaurantId(rs.getInt("restaurant_id"));
                    c.setName(rs.getString("name"));
                    c.setDisplayOrder(rs.getInt("display_order"));
                    c.setImagePath(rs.getString("image_path"));
                    c.setActive(rs.getBoolean("is_active"));
                    c.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    c.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                    result.add(c);
                }
            }
        }
        return result;
    }

    @Override
    public CategoryDTO findById(int categoryId) throws SQLException {
        String sql = SELECT_BASE + "WHERE category_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                CategoryDTO c = new CategoryDTO();
                c.setCategoryId(rs.getInt("category_id"));
                c.setRestaurantId(rs.getInt("restaurant_id"));
                c.setName(rs.getString("name"));
                c.setDisplayOrder(rs.getInt("display_order"));
                c.setImagePath(rs.getString("image_path"));
                c.setActive(rs.getBoolean("is_active"));
                c.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                c.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                return c;
            }
        }
    }

    @Override
    public boolean existsByName(int restaurantId, String name, Integer excludeCategoryId) throws SQLException {
        String sql = "SELECT 1 FROM category WHERE restaurant_id = ? AND name = ? " +
                (excludeCategoryId != null ? "AND category_id <> ?" : "") + " LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, name);
            if (excludeCategoryId != null) {
                ps.setInt(3, excludeCategoryId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int insert(CategoryDTO c) throws SQLException {
        String sql = "INSERT INTO category (restaurant_id, name, display_order, image_path, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getRestaurantId());
            ps.setString(2, c.getName());
            ps.setInt(3, c.getDisplayOrder());
            ps.setString(4, c.getImagePath());
            ps.setBoolean(5, c.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(CategoryDTO c) throws SQLException {
        String sql = "UPDATE category SET name = ?, display_order = ?, image_path = ?, is_active = ? " +
                "WHERE category_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setInt(2, c.getDisplayOrder());
            ps.setString(3, c.getImagePath());
            ps.setBoolean(4, c.isActive());
            ps.setInt(5, c.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

}
