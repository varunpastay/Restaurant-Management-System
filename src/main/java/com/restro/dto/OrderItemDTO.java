package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single line item on an order. {@code foodNameSnapshot} and
 * {@code unitPrice} are captured at order time so a later menu rename,
 * repricing, or deletion of the food item never rewrites this historical row.
 */
public class OrderItemDTO implements Serializable {

    private int orderItemId;
    private int orderId;
    private int foodItemId;
    private String foodNameSnapshot;
    private BigDecimal unitPrice;
    private int quantity;
    private String specialInstructions;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(int foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodNameSnapshot() {
        return foodNameSnapshot;
    }

    public void setFoodNameSnapshot(String foodNameSnapshot) {
        this.foodNameSnapshot = foodNameSnapshot;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
