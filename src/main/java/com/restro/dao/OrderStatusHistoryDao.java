package com.restro.dao;

import com.restro.dto.OrderStatusHistoryDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Read-only: history rows are only ever appended transactionally alongside
 * an order status change (see {@link OrderDao#placeOrder} and
 * {@link OrderDao#updateStatus}), so no standalone insert is exposed here.
 */
public interface OrderStatusHistoryDao {

    /** Oldest first, so it renders directly as a chronological timeline. */
    List<OrderStatusHistoryDTO> findByOrder(int orderId) throws SQLException;
}
