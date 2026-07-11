package com.restro.dao;

import com.restro.dto.OrderDTO;
import com.restro.dto.OrderItemDTO;
import com.restro.dto.OrderStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Order is the aggregate root over its line items and status history within
 * a single order-placement/status-change transaction, so the transactional,
 * multi-table operations below (placeOrder, updateStatus, cancelOrder) live
 * here rather than being split across OrderItemDao/OrderStatusHistoryDao,
 * which stay read-only. See OrderDaoImpl for the transaction boundaries.
 */
public interface OrderDao {

    /**
     * Inserts the order and all of its items plus the initial PENDING
     * history row in one transaction. On return, {@code order} has its
     * generated orderId and orderNo populated. Returns the generated order_id.
     */
    int placeOrder(OrderDTO order, List<OrderItemDTO> items) throws SQLException;

    /** Full order incl. items and table_no, or null if not found. */
    OrderDTO findById(int orderId) throws SQLException;

    /** Full order incl. items and table_no, or null if not found - used by the customer order-tracking page. */
    OrderDTO findByOrderNo(String orderNo) throws SQLException;

    /** Orders not yet served, oldest first (FIFO kitchen queue): PENDING, ACCEPTED, PREPARING, READY. */
    List<OrderDTO> findActiveForKitchen(int restaurantId) throws SQLException;

    /** Orders served but not yet paid, oldest first - the counter's billing queue. */
    List<OrderDTO> findAwaitingBilling(int restaurantId) throws SQLException;

    /** Updates status and appends a history row atomically. changedByStaffId may be null (customer-triggered transitions, if any). */
    boolean updateStatus(int orderId, OrderStatus newStatus, Integer changedByStaffId) throws SQLException;

    boolean cancelOrder(int orderId, Integer changedByStaffId) throws SQLException;
}
