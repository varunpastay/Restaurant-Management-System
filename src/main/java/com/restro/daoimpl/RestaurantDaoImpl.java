package com.restro.daoimpl;

import com.restro.dao.RestaurantDao;
import com.restro.dto.RestaurantDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class RestaurantDaoImpl implements RestaurantDao {

    private static final AppLogger LOG = AppLogger.getLogger(RestaurantDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT restaurant_id, name, logo_path, banner_path, address, phone, email, gstin, " +
            "currency_code, currency_symbol, service_charge_percent, theme_color, dark_mode_default, " +
            "opening_time, closing_time, is_open, created_at, updated_at FROM restaurant ";

    @Override
    public RestaurantDTO findFirst() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY restaurant_id LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            RestaurantDTO r = new RestaurantDTO();
            r.setRestaurantId(rs.getInt("restaurant_id"));
            r.setName(rs.getString("name"));
            r.setLogoPath(rs.getString("logo_path"));
            r.setBannerPath(rs.getString("banner_path"));
            r.setAddress(rs.getString("address"));
            r.setPhone(rs.getString("phone"));
            r.setEmail(rs.getString("email"));
            r.setGstin(rs.getString("gstin"));
            r.setCurrencyCode(rs.getString("currency_code"));
            r.setCurrencySymbol(rs.getString("currency_symbol"));
            r.setServiceChargePercent(rs.getBigDecimal("service_charge_percent"));
            r.setThemeColor(rs.getString("theme_color"));
            r.setDarkModeDefault(rs.getBoolean("dark_mode_default"));
            r.setOpeningTime(rs.getObject("opening_time", LocalTime.class));
            r.setClosingTime(rs.getObject("closing_time", LocalTime.class));
            r.setOpen(rs.getBoolean("is_open"));
            r.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
            r.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
            return r;
        }
    }

    @Override
    public RestaurantDTO findById(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE restaurant_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RestaurantDTO r = new RestaurantDTO();
                r.setRestaurantId(rs.getInt("restaurant_id"));
                r.setName(rs.getString("name"));
                r.setLogoPath(rs.getString("logo_path"));
                r.setBannerPath(rs.getString("banner_path"));
                r.setAddress(rs.getString("address"));
                r.setPhone(rs.getString("phone"));
                r.setEmail(rs.getString("email"));
                r.setGstin(rs.getString("gstin"));
                r.setCurrencyCode(rs.getString("currency_code"));
                r.setCurrencySymbol(rs.getString("currency_symbol"));
                r.setServiceChargePercent(rs.getBigDecimal("service_charge_percent"));
                r.setThemeColor(rs.getString("theme_color"));
                r.setDarkModeDefault(rs.getBoolean("dark_mode_default"));
                r.setOpeningTime(rs.getObject("opening_time", LocalTime.class));
                r.setClosingTime(rs.getObject("closing_time", LocalTime.class));
                r.setOpen(rs.getBoolean("is_open"));
                r.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                r.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                return r;
            }
        }
    }

    @Override
    public boolean update(RestaurantDTO r) throws SQLException {
        String sql = "UPDATE restaurant SET name = ?, logo_path = ?, banner_path = ?, address = ?, phone = ?, " +
                "email = ?, gstin = ?, currency_code = ?, currency_symbol = ?, service_charge_percent = ?, " +
                "theme_color = ?, dark_mode_default = ?, opening_time = ?, closing_time = ?, is_open = ? " +
                "WHERE restaurant_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getLogoPath());
            ps.setString(3, r.getBannerPath());
            ps.setString(4, r.getAddress());
            ps.setString(5, r.getPhone());
            ps.setString(6, r.getEmail());
            ps.setString(7, r.getGstin());
            ps.setString(8, r.getCurrencyCode());
            ps.setString(9, r.getCurrencySymbol());
            ps.setBigDecimal(10, r.getServiceChargePercent());
            ps.setString(11, r.getThemeColor());
            ps.setBoolean(12, r.isDarkModeDefault());
            JdbcUtil.setNullableTime(ps, 13, r.getOpeningTime());
            JdbcUtil.setNullableTime(ps, 14, r.getClosingTime());
            ps.setBoolean(15, r.isOpen());
            ps.setInt(16, r.getRestaurantId());
            LOG.info("Updating restaurant settings for restaurantId=" + r.getRestaurantId());
            return ps.executeUpdate() > 0;
        }
    }

}
