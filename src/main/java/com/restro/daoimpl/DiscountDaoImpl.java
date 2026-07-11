package com.restro.daoimpl;

import com.restro.dao.DiscountDao;
import com.restro.dto.DiscountDTO;
import com.restro.dto.DiscountType;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiscountDaoImpl implements DiscountDao {

    private static final String SELECT_BASE =
            "SELECT discount_id, restaurant_id, code, description, discount_type, value, is_active, " +
            "valid_from, valid_to, created_at FROM discount ";

    @Override
    public List<DiscountDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE restaurant_id = ? ORDER BY code";
        List<DiscountDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiscountDTO d = new DiscountDTO();
                    d.setDiscountId(rs.getInt("discount_id"));
                    d.setRestaurantId(rs.getInt("restaurant_id"));
                    d.setCode(rs.getString("code"));
                    d.setDescription(rs.getString("description"));
                    d.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
                    d.setValue(rs.getBigDecimal("value"));
                    d.setActive(rs.getBoolean("is_active"));
                    d.setValidFrom(JdbcUtil.toLocalDate(rs.getDate("valid_from")));
                    d.setValidTo(JdbcUtil.toLocalDate(rs.getDate("valid_to")));
                    d.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(d);
                }
            }
        }
        return result;
    }

    @Override
    public DiscountDTO findById(int discountId) throws SQLException {
        String sql = SELECT_BASE + "WHERE discount_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, discountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DiscountDTO d = new DiscountDTO();
                d.setDiscountId(rs.getInt("discount_id"));
                d.setRestaurantId(rs.getInt("restaurant_id"));
                d.setCode(rs.getString("code"));
                d.setDescription(rs.getString("description"));
                d.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
                d.setValue(rs.getBigDecimal("value"));
                d.setActive(rs.getBoolean("is_active"));
                d.setValidFrom(JdbcUtil.toLocalDate(rs.getDate("valid_from")));
                d.setValidTo(JdbcUtil.toLocalDate(rs.getDate("valid_to")));
                d.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return d;
            }
        }
    }

    @Override
    public DiscountDTO findActiveByCode(int restaurantId, String code) throws SQLException {
        String sql = SELECT_BASE + "WHERE restaurant_id = ? AND code = ? AND is_active = 1 " +
                "AND (valid_from IS NULL OR valid_from <= CURDATE()) " +
                "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DiscountDTO d = new DiscountDTO();
                d.setDiscountId(rs.getInt("discount_id"));
                d.setRestaurantId(rs.getInt("restaurant_id"));
                d.setCode(rs.getString("code"));
                d.setDescription(rs.getString("description"));
                d.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
                d.setValue(rs.getBigDecimal("value"));
                d.setActive(rs.getBoolean("is_active"));
                d.setValidFrom(JdbcUtil.toLocalDate(rs.getDate("valid_from")));
                d.setValidTo(JdbcUtil.toLocalDate(rs.getDate("valid_to")));
                d.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return d;
            }
        }
    }

    @Override
    public int insert(DiscountDTO d) throws SQLException {
        String sql = "INSERT INTO discount (restaurant_id, code, description, discount_type, value, " +
                "is_active, valid_from, valid_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getRestaurantId());
            ps.setString(2, d.getCode());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getDiscountType().name());
            ps.setBigDecimal(5, d.getValue());
            ps.setBoolean(6, d.isActive());
            JdbcUtil.setNullableDate(ps, 7, d.getValidFrom());
            JdbcUtil.setNullableDate(ps, 8, d.getValidTo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(DiscountDTO d) throws SQLException {
        String sql = "UPDATE discount SET code = ?, description = ?, discount_type = ?, value = ?, " +
                "is_active = ?, valid_from = ?, valid_to = ? WHERE discount_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getCode());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getDiscountType().name());
            ps.setBigDecimal(4, d.getValue());
            ps.setBoolean(5, d.isActive());
            JdbcUtil.setNullableDate(ps, 6, d.getValidFrom());
            JdbcUtil.setNullableDate(ps, 7, d.getValidTo());
            ps.setInt(8, d.getDiscountId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int discountId) throws SQLException {
        String sql = "DELETE FROM discount WHERE discount_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, discountId);
            return ps.executeUpdate() > 0;
        }
    }

}
