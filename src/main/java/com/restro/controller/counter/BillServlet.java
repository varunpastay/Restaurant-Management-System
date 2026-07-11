package com.restro.controller.counter;

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
import com.restro.utility.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * The counter's single order-detail view: full item breakdown, named tax
 * lines, service charge, discount, grand total, and payment status. Used
 * both to review a bill before marking it paid and, after payment, as the
 * printable invoice / reprint screen - looked up by orderId (dashboard
 * cards) or orderNo (the "reprint a past invoice" search box).
 */
@WebServlet(name = "BillServlet", urlPatterns = {"/counter/bill"})
public class BillServlet extends HttpServlet {

    private static final AppLogger LOG = AppLogger.getLogger(BillServlet.class);
    private final OrderDao orderDao = new OrderDaoImpl();
    private final PaymentDao paymentDao = new PaymentDaoImpl();
    private final TaxDao taxDao = new TaxDaoImpl();
    private final RestaurantDao restaurantDao = new RestaurantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int orderId = ValidationUtil.parseIntOrDefault(request.getParameter("orderId"), -1);
        String orderNo = request.getParameter("orderNo");

        try {
            OrderDTO order = ValidationUtil.isNotBlank(orderNo)
                    ? orderDao.findByOrderNo(orderNo.trim())
                    : orderDao.findById(orderId);

            if (order == null) {
                request.setAttribute("reason", "We couldn't find that order.");
                request.getRequestDispatcher("/WEB-INF/jsp/customer/invalid-table.jsp").forward(request, response);
                return;
            }

            PaymentDTO payment = paymentDao.findByOrder(order.getOrderId());
            List<TaxDTO> taxes = taxDao.findActiveByRestaurant(order.getRestaurantId());
            RestaurantDTO restaurant = restaurantDao.findById(order.getRestaurantId());

            request.setAttribute("order", order);
            request.setAttribute("payment", payment);
            request.setAttribute("taxes", taxes);
            request.setAttribute("restaurant", restaurant);
            request.getRequestDispatcher("/WEB-INF/jsp/counter/bill.jsp").forward(request, response);
        } catch (SQLException e) {
            LOG.error("Failed to load bill for orderId=" + orderId + " orderNo=" + orderNo, e);
            throw new ServletException("Failed to load bill", e);
        }
    }
}
