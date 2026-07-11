package com.restro.daoimpl;

import com.restro.dao.SettingsDao;
import com.restro.dto.SettingsDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettingsDaoImpl implements SettingsDao {

    @Override
    public List<SettingsDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        String sql = "SELECT setting_id, restaurant_id, setting_key, setting_value FROM settings " +
                "WHERE restaurant_id = ? ORDER BY setting_key";
        List<SettingsDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SettingsDTO s = new SettingsDTO();
                    s.setSettingId(rs.getInt("setting_id"));
                    s.setRestaurantId(rs.getInt("restaurant_id"));
                    s.setSettingKey(rs.getString("setting_key"));
                    s.setSettingValue(rs.getString("setting_value"));
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override
    public String findValue(int restaurantId, String key) throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE restaurant_id = ? AND setting_key = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("setting_value") : null;
            }
        }
    }

    @Override
    public void upsert(int restaurantId, String key, String value) throws SQLException {
        String sql = "INSERT INTO settings (restaurant_id, setting_key, setting_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, key);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

}
