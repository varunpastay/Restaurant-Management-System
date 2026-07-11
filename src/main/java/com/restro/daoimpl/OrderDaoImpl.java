package com.restro.daoimpl;

import com.restro.dao.OrderDao;
import com.restro.dao.OrderItemDao;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderItemDTO;
import com.restro.dto.OrderStatus;
import com.restro.utility.AppLogger;
import com.restro.utility.DBConnectionUtil;
import com.restro.utility.OrderNumberUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Order is the transactional aggregate root over order_item and
 * order_status_history: placeOrder/updateStatus/cancelOrder each open one
 * connection, turn off auto-commit, write to all affected tables, and
 * commit/rollback together. Plain reads (findById and friends) don't need a
 * shared transaction, so they compose with {@link OrderItemDao} instead of
 * duplicating its query.
 */
public class OrderDaoImpl implements OrderDao {

    private static final AppLogger LOG = AppLogger.getLogger(OrderDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT o.order_id, o.restaurant_id, o.order_no, o.table_id, o.status, o.subtotal, o.tax_amount, " +
            "o.service_charge_amount, o.discount_amount, o.grand_total, o.discount_id, o.customer_note, " +
            "o.created_at, o.updated_at, t.table_no " +
            "FROM orders o JOIN restaurant_table t ON t.table_id = o.table_id ";

    private final OrderItemDao orderItemDao = new OrderItemDaoImpl();

    @Override
    public int placeOrder(OrderDTO order, List<OrderItemDTO> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item");
        }
        String insertOrderSql = "INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, " +
                "tax_amount, service_charge_amount, discount_amount, grand_total, discount_id, customer_note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, " +
                "quantity, special_instructions, line_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertHistorySql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, order.getRestaurantId());
                    ps.setString(2, "PENDING-" + System.nanoTime());
                    ps.setInt(3, order.getTableId());
                    ps.setString(4, OrderStatus.PENDING.name());
                    ps.setBigDecimal(5, order.getSubtotal());
                    ps.setBigDecimal(6, order.getTaxAmount());
                    ps.setBigDecimal(7, order.getServiceChargeAmount());
                    ps.setBigDecimal(8, order.getDiscountAmount());
                    ps.setBigDecimal(9, order.getGrandTotal());
                    JdbcUtil.setNullableInt(ps, 10, order.getDiscountId());
                    ps.setString(11, order.getCustomerNote());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        orderId = keys.next() ? keys.getInt(1) : 0;
                    }
                }

                String orderNo = OrderNumberUtil.format(LocalDate.now(), orderId);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET order_no = ? WHERE order_id = ?")) {
                    ps.setString(1, orderNo);
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                    for (OrderItemDTO item : items) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, item.getFoodItemId());
                        ps.setString(3, item.getFoodNameSnapshot());
                        ps.setBigDecimal(4, item.getUnitPrice());
                        ps.setInt(5, item.getQuantity());
                        ps.setString(6, item.getSpecialInstructions());
                        ps.setBigDecimal(7, item.getLineTotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertHistorySql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, OrderStatus.PENDING.name());
                    ps.executeUpdate();
                }

                conn.commit();
                order.setOrderId(orderId);
                order.setOrderNo(orderNo);
                order.setStatus(OrderStatus.PENDING);
                LOG.info("Placed order " + orderNo + " (orderId=" + orderId + ", tableId=" + order.getTableId() + ")");
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public OrderDTO findById(int orderId) throws SQLException {
        return findOne(SELECT_BASE + "WHERE o.order_id = ?", orderId, null);
    }

    @Override
    public OrderDTO findByOrderNo(String orderNo) throws SQLException {
        return findOne(SELECT_BASE + "WHERE o.order_no = ?", 0, orderNo);
    }

    private OrderDTO findOne(String sql, int orderId, String orderNo) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (orderNo != null) {
                ps.setString(1, orderNo);
            } else {
                ps.setInt(1, orderId);
            }
            OrderDTO order;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                order = new OrderDTO();
                order.setOrderId(rs.getInt("order_id"));
                order.setRestaurantId(rs.getInt("restaurant_id"));
                order.setOrderNo(rs.getString("order_no"));
                order.setTableId(rs.getInt("table_id"));
                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                order.setSubtotal(rs.getBigDecimal("subtotal"));
                order.setTaxAmount(rs.getBigDecimal("tax_amount"));
                order.setServiceChargeAmount(rs.getBigDecimal("service_charge_amount"));
                order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                order.setGrandTotal(rs.getBigDecimal("grand_total"));
                order.setDiscountId((Integer) rs.getObject("discount_id"));
                order.setCustomerNote(rs.getString("customer_note"));
                order.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                order.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                order.setTableNo(rs.getString("table_no"));
            }
            order.setItems(orderItemDao.findByOrder(order.getOrderId()));
            return order;
        }
    }

    @Override
    public List<OrderDTO> findActiveForKitchen(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.restaurant_id = ? " +
                "AND o.status IN ('PENDING','ACCEPTED','PREPARING','READY') ORDER BY o.created_at ASC";
        return findList(sql, restaurantId);
    }

    @Override
    public List<OrderDTO> findAwaitingBilling(int restaurantId) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.restaurant_id = ? AND o.status = 'SERVED' ORDER BY o.created_at ASC";
        return findList(sql, restaurantId);
    }

    private List<OrderDTO> findList(String sql, int restaurantId) throws SQLException {
        List<OrderDTO> orders = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDTO o = new OrderDTO();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setRestaurantId(rs.getInt("restaurant_id"));
                    o.setOrderNo(rs.getString("order_no"));
                    o.setTableId(rs.getInt("table_id"));
                    o.setStatus(OrderStatus.valueOf(rs.getString("status")));
                    o.setSubtotal(rs.getBigDecimal("subtotal"));
                    o.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    o.setServiceChargeAmount(rs.getBigDecimal("service_charge_amount"));
                    o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    o.setGrandTotal(rs.getBigDecimal("grand_total"));
                    o.setDiscountId((Integer) rs.getObject("discount_id"));
                    o.setCustomerNote(rs.getString("customer_note"));
                    o.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                    o.setUpdatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
                    o.setTableNo(rs.getString("table_no"));
                    orders.add(o);
                }
            }
        }
        for (OrderDTO order : orders) {
            order.setItems(orderItemDao.findByOrder(order.getOrderId()));
        }
        return orders;
    }

    @Override
    public boolean updateStatus(int orderId, OrderStatus newStatus, Integer changedByStaffId) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean updated;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = ? WHERE order_id = ?")) {
                    ps.setString(1, newStatus.name());
                    ps.setInt(2, orderId);
                    updated = ps.executeUpdate() > 0;
                }
                if (updated) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO order_status_history (order_id, status, changed_by_staff_id) VALUES (?, ?, ?)")) {
                        ps.setInt(1, orderId);
                        ps.setString(2, newStatus.name());
                        JdbcUtil.setNullableInt(ps, 3, changedByStaffId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean cancelOrder(int orderId, Integer changedByStaffId) throws SQLException {
        return updateStatus(orderId, OrderStatus.CANCELLED, changedByStaffId);
    }

}
