package com.restro.service;

import com.restro.dao.OrderDao;
import com.restro.dao.PaymentDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.PaymentDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderStatus;
import com.restro.dto.PaymentDTO;
import com.restro.dto.PaymentMethod;
import com.restro.dto.PaymentStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Orchestrates "mark paid": inserting the payment row and completing the
 * order are two separate DAO calls (not one shared transaction - see
 * PaymentDaoImpl/OrderDaoImpl), so this method is written to be safely
 * retryable rather than strictly atomic. If the order-completion step ever
 * failed after the payment insert succeeded, calling this again finds the
 * existing payment (order_id is UNIQUE) and just finishes the status
 * update - a counter clerk re-clicking "Mark Paid" can never double-charge.
 */
public class BillingService {

    private final OrderDao orderDao = new OrderDaoImpl();
    private final PaymentDao paymentDao = new PaymentDaoImpl();

    public PaymentDTO markPaid(int orderId, PaymentMethod method, Integer staffId) throws SQLException {
        OrderDTO order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        if (order.getStatus() != OrderStatus.SERVED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only served orders can be marked paid (order " + order.getOrderNo()
                            + " is currently " + order.getStatus() + ")");
        }

        PaymentDTO payment = paymentDao.findByOrder(orderId);
        if (payment == null) {
            payment = new PaymentDTO();
            payment.setOrderId(orderId);
            payment.setAmount(order.getGrandTotal());
            payment.setMethod(method);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            paymentDao.insert(payment);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            orderDao.updateStatus(orderId, OrderStatus.COMPLETED, staffId);
        }
        return payment;
    }
}
