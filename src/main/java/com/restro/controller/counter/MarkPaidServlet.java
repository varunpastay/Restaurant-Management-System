package com.restro.controller.counter;

import com.restro.dto.PaymentDTO;
import com.restro.dto.PaymentMethod;
import com.restro.filters.StaffAuthFilter;
import com.restro.service.BillingService;
import com.restro.utility.AppLogger;
import com.restro.utility.JsonResponseUtil;
import com.restro.utility.JsonUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "MarkPaidServlet", urlPatterns = {"/counter/orders/pay"})
public class MarkPaidServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(MarkPaidServlet.class);
    private final BillingService billingService = new BillingService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        String methodParam = request.getParameter("method");
        HttpSession session = request.getSession(false);
        Integer staffId = session != null ? (Integer) session.getAttribute(StaffAuthFilter.SESSION_STAFF_ID) : null;

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodParam == null ? "CASH" : methodParam);
        } catch (IllegalArgumentException e) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown payment method");
            return;
        }

        try {
            PaymentDTO payment = billingService.markPaid(orderId, method, staffId);
            LOG.info("Order " + orderId + " marked paid via " + method + " (invoice " + payment.getInvoiceNo() + ")");
            JsonResponseUtil.writeJson(response, "{\"orderId\":" + orderId + ",\"invoiceNo\":"
                    + JsonUtil.quote(payment.getInvoiceNo()) + ",\"status\":\"COMPLETED\"}");
        } catch (IllegalArgumentException e) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (SQLException e) {
            LOG.error("Failed to mark order " + orderId + " paid", e);
            JsonResponseUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
