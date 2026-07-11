package com.restro.service;

import com.restro.dao.OrderDao;
import com.restro.dao.TaxDao;
import com.restro.daoimpl.OrderDaoImpl;
import com.restro.daoimpl.TaxDaoImpl;
import com.restro.dto.CartDTO;
import com.restro.dto.CartItemDTO;
import com.restro.dto.DiscountDTO;
import com.restro.dto.DiscountType;
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderItemDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.TaxDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin orchestration above the DAO layer: computes an order's money
 * breakdown (subtotal, tax, service charge, discount, grand total) from the
 * cart and the restaurant's current configuration, then hands the assembled
 * OrderDTO/OrderItemDTOs to OrderDao.placeOrder for the transactional write.
 * Keeps this arithmetic out of both the JSP layer and the DAO layer.
 */
public class OrderPlacementService {

    private static final int MONEY_SCALE = 2;

    private final OrderDao orderDao = new OrderDaoImpl();
    private final TaxDao taxDao = new TaxDaoImpl();

    public OrderDTO placeOrder(CartDTO cart, RestaurantDTO restaurant, DiscountDTO discount, String customerNote)
            throws SQLException {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty cart");
        }

        BigDecimal subtotal = round(cart.getSubtotal());

        BigDecimal taxPercent = taxDao.findActiveByRestaurant(restaurant.getRestaurantId()).stream()
                .map(TaxDTO::getPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = round(subtotal.multiply(taxPercent).divide(BigDecimal.valueOf(100)));

        BigDecimal serviceChargePercent = restaurant.getServiceChargePercent() != null
                ? restaurant.getServiceChargePercent() : BigDecimal.ZERO;
        BigDecimal serviceChargeAmount = round(subtotal.multiply(serviceChargePercent).divide(BigDecimal.valueOf(100)));

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null) {
            discountAmount = discount.getDiscountType() == DiscountType.PERCENT
                    ? round(subtotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100)))
                    : round(discount.getValue());
            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal;
            }
        }

        BigDecimal grandTotal = subtotal.add(taxAmount).add(serviceChargeAmount).subtract(discountAmount);

        OrderDTO order = new OrderDTO();
        order.setRestaurantId(restaurant.getRestaurantId());
        order.setTableId(cart.getTableId());
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setServiceChargeAmount(serviceChargeAmount);
        order.setDiscountAmount(discountAmount);
        order.setGrandTotal(grandTotal);
        order.setDiscountId(discount != null ? discount.getDiscountId() : null);
        order.setCustomerNote(customerNote);

        List<OrderItemDTO> items = new ArrayList<>();
        for (CartItemDTO cartItem : cart.getItems()) {
            OrderItemDTO item = new OrderItemDTO();
            item.setFoodItemId(cartItem.getFoodItemId());
            item.setFoodNameSnapshot(cartItem.getName());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setSpecialInstructions(cartItem.getSpecialInstructions());
            item.setLineTotal(round(cartItem.getLineTotal()));
            items.add(item);
        }

        orderDao.placeOrder(order, items);
        return order;
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
