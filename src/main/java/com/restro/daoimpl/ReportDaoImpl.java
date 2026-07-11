package com.restro.daoimpl;

import com.restro.dao.ReportDao;
import com.restro.dto.DailySalesDTO;
import com.restro.dto.FoodSalesDTO;
import com.restro.dto.HourlySalesDTO;
import com.restro.utility.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl implements ReportDao {

    @Override
    public BigDecimal getTotalRevenue(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM orders " +
                "WHERE restaurant_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    @Override
    public int getOrderCount(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders " +
                "WHERE restaurant_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public List<FoodSalesDTO> getTopSellingFoods(int restaurantId, LocalDateTime from, LocalDateTime to, int limit) throws SQLException {
        return getFoodSales(restaurantId, from, to, limit, "DESC");
    }

    @Override
    public List<FoodSalesDTO> getLeastSellingFoods(int restaurantId, LocalDateTime from, LocalDateTime to, int limit) throws SQLException {
        return getFoodSales(restaurantId, from, to, limit, "ASC");
    }

    private List<FoodSalesDTO> getFoodSales(int restaurantId, LocalDateTime from, LocalDateTime to, int limit, String direction)
            throws SQLException {
        String sql = "SELECT oi.food_item_id, MAX(oi.food_name_snapshot) AS name, " +
                "SUM(oi.quantity) AS total_qty, SUM(oi.line_total) AS total_revenue " +
                "FROM order_item oi JOIN orders o ON o.order_id = oi.order_id " +
                "WHERE o.restaurant_id = ? AND o.status = 'COMPLETED' AND o.created_at >= ? AND o.created_at < ? " +
                "GROUP BY oi.food_item_id ORDER BY total_qty " + direction + " LIMIT ?";
        List<FoodSalesDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodSalesDTO row = new FoodSalesDTO();
                    row.setFoodItemId(rs.getInt("food_item_id"));
                    row.setName(rs.getString("name"));
                    row.setTotalQuantity(rs.getInt("total_qty"));
                    row.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    @Override
    public List<HourlySalesDTO> getHourlySales(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT HOUR(created_at) AS hr, COUNT(*) AS order_count, SUM(grand_total) AS revenue " +
                "FROM orders WHERE restaurant_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ? " +
                "GROUP BY HOUR(created_at) ORDER BY hr";
        List<HourlySalesDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HourlySalesDTO row = new HourlySalesDTO();
                    row.setHour(rs.getInt("hr"));
                    row.setOrderCount(rs.getInt("order_count"));
                    row.setRevenue(rs.getBigDecimal("revenue"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    @Override
    public List<DailySalesDTO> getDailySales(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT DATE(created_at) AS day, COUNT(*) AS order_count, SUM(grand_total) AS revenue " +
                "FROM orders WHERE restaurant_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ? " +
                "GROUP BY DATE(created_at) ORDER BY day";
        List<DailySalesDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailySalesDTO row = new DailySalesDTO();
                    row.setDate(JdbcUtil.toLocalDate(rs.getDate("day")));
                    row.setOrderCount(rs.getInt("order_count"));
                    row.setRevenue(rs.getBigDecimal("revenue"));
                    result.add(row);
                }
            }
        }
        return result;
    }
}
