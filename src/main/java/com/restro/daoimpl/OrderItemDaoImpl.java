package com.restro.daoimpl;

import com.restro.dao.OrderItemDao;
import com.restro.dto.OrderItemDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDaoImpl implements OrderItemDao {

    @Override
    public List<OrderItemDTO> findByOrder(int orderId) throws SQLException {
        String sql = "SELECT order_item_id, order_id, food_item_id, food_name_snapshot, unit_price, " +
                "quantity, special_instructions, line_total, created_at FROM order_item " +
                "WHERE order_id = ? ORDER BY order_item_id";
        List<OrderItemDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemDTO item = new OrderItemDTO();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setFoodItemId(rs.getInt("food_item_id"));
                    item.setFoodNameSnapshot(rs.getString("food_name_snapshot"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSpecialInstructions(rs.getString("special_instructions"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    item.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    result.add(item);
                }
            }
        }
        return result;
    }
}
