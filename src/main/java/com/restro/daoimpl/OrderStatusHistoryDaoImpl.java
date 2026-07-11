package com.restro.daoimpl;

import com.restro.dao.OrderStatusHistoryDao;
import com.restro.dto.OrderStatus;
import com.restro.dto.OrderStatusHistoryDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderStatusHistoryDaoImpl implements OrderStatusHistoryDao {

    @Override
    public List<OrderStatusHistoryDTO> findByOrder(int orderId) throws SQLException {
        String sql = "SELECT history_id, order_id, status, changed_at, changed_by_staff_id " +
                "FROM order_status_history WHERE order_id = ? ORDER BY changed_at ASC, history_id ASC";
        List<OrderStatusHistoryDTO> result = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistoryDTO h = new OrderStatusHistoryDTO();
                    h.setHistoryId(rs.getInt("history_id"));
                    h.setOrderId(rs.getInt("order_id"));
                    h.setStatus(OrderStatus.valueOf(rs.getString("status")));
                    h.setChangedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("changed_at")));
                    h.setChangedByStaffId((Integer) rs.getObject("changed_by_staff_id"));
                    result.add(h);
                }
            }
        }
        return result;
    }
}
