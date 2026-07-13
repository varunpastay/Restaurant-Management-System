package com.restro.daoimpl;

import com.restro.dao.AdminDao;
import com.restro.dto.AdminDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AdminDaoImpl implements AdminDao {

    private static final String SELECT_BASE =
            "SELECT admin_id, restaurant_id, email, password_hash, password_salt, full_name, " +
            "is_active, last_login_at, created_at FROM admin ";

    @Override
    public AdminDTO findByEmail(String email) throws SQLException {
        String sql = SELECT_BASE + "WHERE email = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                AdminDTO a = new AdminDTO();
                a.setAdminId(rs.getInt("admin_id"));
                a.setRestaurantId(rs.getInt("restaurant_id"));
                a.setEmail(rs.getString("email"));
                a.setPasswordHash(rs.getString("password_hash"));
                a.setPasswordSalt(rs.getString("password_salt"));
                a.setFullName(rs.getString("full_name"));
                a.setActive(rs.getBoolean("is_active"));
                a.setLastLoginAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("last_login_at")));
                a.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return a;
            }
        }
    }

    @Override
    public AdminDTO findById(int adminId) throws SQLException {
        String sql = SELECT_BASE + "WHERE admin_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                AdminDTO a = new AdminDTO();
                a.setAdminId(rs.getInt("admin_id"));
                a.setRestaurantId(rs.getInt("restaurant_id"));
                a.setEmail(rs.getString("email"));
                a.setPasswordHash(rs.getString("password_hash"));
                a.setPasswordSalt(rs.getString("password_salt"));
                a.setFullName(rs.getString("full_name"));
                a.setActive(rs.getBoolean("is_active"));
                a.setLastLoginAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("last_login_at")));
                a.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return a;
            }
        }
    }

    @Override
    public boolean update(AdminDTO admin) throws SQLException {
        String sql = "UPDATE admin SET full_name = ?, email = ? WHERE admin_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getFullName());
            ps.setString(2, admin.getEmail());
            ps.setInt(3, admin.getAdminId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(int adminId, String passwordHash, String passwordSalt) throws SQLException {
        String sql = "UPDATE admin SET password_hash = ?, password_salt = ? WHERE admin_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, passwordSalt);
            ps.setInt(3, adminId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateLastLogin(int adminId, LocalDateTime loginTime) throws SQLException {
        String sql = "UPDATE admin SET last_login_at = ? WHERE admin_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setNullableTimestamp(ps, 1, loginTime);
            ps.setInt(2, adminId);
            return ps.executeUpdate() > 0;
        }
    }

}
