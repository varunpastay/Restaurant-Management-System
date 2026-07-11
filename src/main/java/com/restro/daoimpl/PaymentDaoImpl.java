package com.restro.daoimpl;

import com.restro.dao.PaymentDao;
import com.restro.dto.PaymentDTO;
import com.restro.dto.PaymentMethod;
import com.restro.dto.PaymentStatus;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PaymentDaoImpl implements PaymentDao {

    private static final String SELECT_BASE =
            "SELECT payment_id, order_id, invoice_no, amount, method, payment_status, paid_at, created_at " +
            "FROM payment ";

    @Override
    public PaymentDTO findByOrder(int orderId) throws SQLException {
        String sql = SELECT_BASE + "WHERE order_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                PaymentDTO p = new PaymentDTO();
                p.setPaymentId(rs.getInt("payment_id"));
                p.setOrderId(rs.getInt("order_id"));
                p.setInvoiceNo(rs.getString("invoice_no"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
                p.setPaidAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("paid_at")));
                p.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return p;
            }
        }
    }

    @Override
    public PaymentDTO findByInvoiceNo(String invoiceNo) throws SQLException {
        String sql = SELECT_BASE + "WHERE invoice_no = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                PaymentDTO p = new PaymentDTO();
                p.setPaymentId(rs.getInt("payment_id"));
                p.setOrderId(rs.getInt("order_id"));
                p.setInvoiceNo(rs.getString("invoice_no"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
                p.setPaidAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("paid_at")));
                p.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return p;
            }
        }
    }

    @Override
    public int insert(PaymentDTO p) throws SQLException {
        // invoice_no is filled in with a temporary placeholder, then rewritten to a readable,
        // date-stamped code derived from the generated payment_id - mirrors OrderDaoImpl's
        // order_no strategy so numbering is race-free without a separate counter table.
        String insertSql = "INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int paymentId;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, p.getOrderId());
                    ps.setString(2, "PENDING-" + System.nanoTime());
                    ps.setBigDecimal(3, p.getAmount());
                    ps.setString(4, p.getMethod().name());
                    ps.setString(5, p.getPaymentStatus().name());
                    JdbcUtil.setNullableTimestamp(ps, 6, p.getPaidAt());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        paymentId = keys.next() ? keys.getInt(1) : 0;
                    }
                }
                String invoiceNo = com.restro.utility.InvoiceNumberUtil.format(java.time.LocalDate.now(), paymentId);
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE payment SET invoice_no = ? WHERE payment_id = ?")) {
                    update.setString(1, invoiceNo);
                    update.setInt(2, paymentId);
                    update.executeUpdate();
                }
                p.setInvoiceNo(invoiceNo);
                conn.commit();
                return paymentId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

}
