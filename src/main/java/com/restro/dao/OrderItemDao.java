package com.restro.dao;

import com.restro.dto.OrderItemDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Read-only outside of order placement: order_item rows are only ever
 * created transactionally alongside their parent order (see
 * {@link OrderDao#placeOrder}), so no standalone insert is exposed here.
 */
public interface OrderItemDao {

    List<OrderItemDTO> findByOrder(int orderId) throws SQLException;
}
