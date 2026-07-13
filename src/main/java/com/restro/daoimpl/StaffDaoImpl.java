package com.restro.daoimpl;

import com.restro.dao.StaffDao;
import com.restro.dto.StaffDTO;
import com.restro.dto.StaffRole;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffDaoImpl implements StaffDao {

    private static final String SELECT_BASE =
            "SELECT staff_id, restaurant_id, email, password_hash, password_salt, full_name, role, phone, " +
            "is_active, created_at FROM staff ";

    @Override
    public List<StaffDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE restaurant_id = ? ORDER BY full_name";
        List<StaffDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffDTO s = new StaffDTO();
                    s.setStaffId(rs.getInt("staff_id"));
                    s.setRestaurantId(rs.getInt("restaurant_id"));
                    s.setEmail(rs.getString("email"));
                    s.setPasswordHash(rs.getString("password_hash"));
                    s.setPasswordSalt(rs.getString("password_salt"));
                    s.setFullName(rs.getString("full_name"));
                    s.setRole(StaffRole.valueOf(rs.getString("role")));
                    s.setPhone(rs.getString("phone"));
                    s.setActive(rs.getBoolean("is_active"));
                    s.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override
    public StaffDTO findById(int staffId) throws SQLException {
        String sql = SELECT_BASE + "WHERE staff_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StaffDTO s = new StaffDTO();
                s.setStaffId(rs.getInt("staff_id"));
                s.setRestaurantId(rs.getInt("restaurant_id"));
                s.setEmail(rs.getString("email"));
                s.setPasswordHash(rs.getString("password_hash"));
                s.setPasswordSalt(rs.getString("password_salt"));
                s.setFullName(rs.getString("full_name"));
                s.setRole(StaffRole.valueOf(rs.getString("role")));
                s.setPhone(rs.getString("phone"));
                s.setActive(rs.getBoolean("is_active"));
                s.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return s;
            }
        }
    }

    @Override
    public StaffDTO findByEmail(String email) throws SQLException {
        String sql = SELECT_BASE + "WHERE email = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StaffDTO s = new StaffDTO();
                s.setStaffId(rs.getInt("staff_id"));
                s.setRestaurantId(rs.getInt("restaurant_id"));
                s.setEmail(rs.getString("email"));
                s.setPasswordHash(rs.getString("password_hash"));
                s.setPasswordSalt(rs.getString("password_salt"));
                s.setFullName(rs.getString("full_name"));
                s.setRole(StaffRole.valueOf(rs.getString("role")));
                s.setPhone(rs.getString("phone"));
                s.setActive(rs.getBoolean("is_active"));
                s.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return s;
            }
        }
    }

    @Override
    public int insert(StaffDTO s) throws SQLException {
        String sql = "INSERT INTO staff (restaurant_id, email, password_hash, password_salt, full_name, " +
                "role, phone, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getRestaurantId());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPasswordHash());
            ps.setString(4, s.getPasswordSalt());
            ps.setString(5, s.getFullName());
            ps.setString(6, s.getRole().name());
            ps.setString(7, s.getPhone());
            ps.setBoolean(8, s.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(StaffDTO s) throws SQLException {
        String sql = "UPDATE staff SET email = ?, full_name = ?, role = ?, phone = ?, is_active = ? " +
                "WHERE staff_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getEmail());
            ps.setString(2, s.getFullName());
            ps.setString(3, s.getRole().name());
            ps.setString(4, s.getPhone());
            ps.setBoolean(5, s.isActive());
            ps.setInt(6, s.getStaffId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int staffId) throws SQLException {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(int staffId, String passwordHash, String passwordSalt) throws SQLException {
        String sql = "UPDATE staff SET password_hash = ?, password_salt = ? WHERE staff_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, passwordSalt);
            ps.setInt(3, staffId);
            return ps.executeUpdate() > 0;
        }
    }

}
