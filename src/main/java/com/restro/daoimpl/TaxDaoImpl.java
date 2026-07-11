package com.restro.daoimpl;

import com.restro.dao.TaxDao;
import com.restro.dto.TaxDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaxDaoImpl implements TaxDao {

    private static final String SELECT_BASE =
            "SELECT tax_id, restaurant_id, name, percent, is_active, created_at FROM tax ";

    @Override
    public List<TaxDTO> findAllByRestaurant(int restaurantId) throws SQLException {
        return queryList(SELECT_BASE + "WHERE restaurant_id = ? ORDER BY name", restaurantId);
    }

    @Override
    public List<TaxDTO> findActiveByRestaurant(int restaurantId) throws SQLException {
        return queryList(SELECT_BASE + "WHERE restaurant_id = ? AND is_active = 1 ORDER BY name", restaurantId);
    }

    private List<TaxDTO> queryList(String sql, int restaurantId) throws SQLException {
        List<TaxDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TaxDTO t = new TaxDTO();
                    t.setTaxId(rs.getInt("tax_id"));
                    t.setRestaurantId(rs.getInt("restaurant_id"));
                    t.setName(rs.getString("name"));
                    t.setPercent(rs.getBigDecimal("percent"));
                    t.setActive(rs.getBoolean("is_active"));
                    t.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public TaxDTO findById(int taxId) throws SQLException {
        String sql = SELECT_BASE + "WHERE tax_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taxId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TaxDTO t = new TaxDTO();
                t.setTaxId(rs.getInt("tax_id"));
                t.setRestaurantId(rs.getInt("restaurant_id"));
                t.setName(rs.getString("name"));
                t.setPercent(rs.getBigDecimal("percent"));
                t.setActive(rs.getBoolean("is_active"));
                t.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return t;
            }
        }
    }

    @Override
    public int insert(TaxDTO tax) throws SQLException {
        String sql = "INSERT INTO tax (restaurant_id, name, percent, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tax.getRestaurantId());
            ps.setString(2, tax.getName());
            ps.setBigDecimal(3, tax.getPercent());
            ps.setBoolean(4, tax.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(TaxDTO tax) throws SQLException {
        String sql = "UPDATE tax SET name = ?, percent = ?, is_active = ? WHERE tax_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tax.getName());
            ps.setBigDecimal(2, tax.getPercent());
            ps.setBoolean(3, tax.isActive());
            ps.setInt(4, tax.getTaxId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int taxId) throws SQLException {
        String sql = "DELETE FROM tax WHERE tax_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taxId);
            return ps.executeUpdate() > 0;
        }
    }

}
