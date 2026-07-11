package com.restro.daoimpl;

import com.restro.dao.RestaurantTableDao;
import com.restro.dto.RestaurantTableDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RestaurantTableDaoImpl implements RestaurantTableDao {

    private static final String SELECT_BASE =
            "SELECT table_id, restaurant_id, table_no, capacity, qr_token, is_active, created_at " +
            "FROM restaurant_table ";

    @Override
    public List<RestaurantTableDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE restaurant_id = ? ORDER BY table_no";
        List<RestaurantTableDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RestaurantTableDTO t = new RestaurantTableDTO();
                    t.setTableId(rs.getInt("table_id"));
                    t.setRestaurantId(rs.getInt("restaurant_id"));
                    t.setTableNo(rs.getString("table_no"));
                    t.setCapacity(rs.getInt("capacity"));
                    t.setQrToken(rs.getString("qr_token"));
                    t.setActive(rs.getBoolean("is_active"));
                    t.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public RestaurantTableDTO findById(int tableId) throws SQLException {
        String sql = SELECT_BASE + "WHERE table_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RestaurantTableDTO t = new RestaurantTableDTO();
                t.setTableId(rs.getInt("table_id"));
                t.setRestaurantId(rs.getInt("restaurant_id"));
                t.setTableNo(rs.getString("table_no"));
                t.setCapacity(rs.getInt("capacity"));
                t.setQrToken(rs.getString("qr_token"));
                t.setActive(rs.getBoolean("is_active"));
                t.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return t;
            }
        }
    }

    @Override
    public RestaurantTableDTO findByToken(String qrToken) throws SQLException {
        String sql = SELECT_BASE + "WHERE qr_token = ? AND is_active = 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qrToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RestaurantTableDTO t = new RestaurantTableDTO();
                t.setTableId(rs.getInt("table_id"));
                t.setRestaurantId(rs.getInt("restaurant_id"));
                t.setTableNo(rs.getString("table_no"));
                t.setCapacity(rs.getInt("capacity"));
                t.setQrToken(rs.getString("qr_token"));
                t.setActive(rs.getBoolean("is_active"));
                t.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return t;
            }
        }
    }

    @Override
    public boolean existsByTableNo(int restaurantId, String tableNo, Integer excludeTableId) throws SQLException {
        String sql = "SELECT 1 FROM restaurant_table WHERE restaurant_id = ? AND table_no = ? " +
                (excludeTableId != null ? "AND table_id <> ?" : "") + " LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, tableNo);
            if (excludeTableId != null) {
                ps.setInt(3, excludeTableId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int insert(RestaurantTableDTO t) throws SQLException {
        String sql = "INSERT INTO restaurant_table (restaurant_id, table_no, capacity, qr_token, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getRestaurantId());
            ps.setString(2, t.getTableNo());
            ps.setInt(3, t.getCapacity());
            ps.setString(4, t.getQrToken());
            ps.setBoolean(5, t.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(RestaurantTableDTO t) throws SQLException {
        String sql = "UPDATE restaurant_table SET table_no = ?, capacity = ?, is_active = ? WHERE table_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTableNo());
            ps.setInt(2, t.getCapacity());
            ps.setBoolean(3, t.isActive());
            ps.setInt(4, t.getTableId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int tableId) throws SQLException {
        String sql = "DELETE FROM restaurant_table WHERE table_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            return ps.executeUpdate() > 0;
        }
    }

}
