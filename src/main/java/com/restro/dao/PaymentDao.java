package com.restro.dao;

import com.restro.dto.PaymentDTO;

import java.sql.SQLException;

public interface PaymentDao {

    /** Null if the order hasn't been paid yet. */
    PaymentDTO findByOrder(int orderId) throws SQLException;

    PaymentDTO findByInvoiceNo(String invoiceNo) throws SQLException;

    /** Standalone insert is safe here: marking an order paid is its own independent action, not part of the order-placement transaction. */
    int insert(PaymentDTO payment) throws SQLException;
}
