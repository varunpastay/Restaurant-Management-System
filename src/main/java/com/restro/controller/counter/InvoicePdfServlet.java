package com.restro.controller.counter;

import com.lowagie.text.DocumentException;
import com.restro.dao.OrderDao;
import com.restro.dao.PaymentDao;
import com.restro.dao.RestaurantDao;
import com.restro.dao.TaxDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.PaymentDaoImpl;
import com.restro.daoimpl.RestaurantDaoImpl;
import com.restro.daoimpl.TaxDaoImpl;
import com.restro.dto.OrderDTO;
import com.restro.dto.PaymentDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.TaxDTO;
import com.restro.utility.AppLogger;
import com.restro.utility.PdfInvoiceUtil;
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Streams a downloadable/printable PDF bill or tax invoice for one order - the "Print Invoice" / "Reprint Invoice" action. */
@WebServlet(name = "InvoicePdfServlet", urlPatterns = {"/counter/invoice.pdf"})
public class InvoicePdfServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(InvoicePdfServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();
    private final PaymentDao paymentDao = new PaymentDaoImpl();
    private final TaxDao taxDao = new TaxDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        try {
            OrderDTO order = orderDao.findById(orderId);
            if (order == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            RestaurantDTO restaurant = restaurantDao.findById(order.getRestaurantId());
            PaymentDTO payment = paymentDao.findByOrder(orderId);
            List<TaxDTO> taxes = taxDao.findActiveByRestaurant(order.getRestaurantId());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"" + order.getOrderNo() + ".pdf\"");
            PdfInvoiceUtil.generate(response.getOutputStream(), restaurant, order, taxes, payment);
        } catch (SQLException | DocumentException e) {
            LOG.error("Failed to generate invoice PDF for orderId=" + orderId, e);
            throw new ServletException("Failed to generate invoice PDF", e);
        }
    }
}
