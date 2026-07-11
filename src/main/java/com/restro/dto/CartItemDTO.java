package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One line in a customer's session-scoped cart. Not a database entity -
 * lives only in HttpSession until the order is placed, at which point its
 * fields seed an {@link OrderItemDTO} snapshot. unitPrice is always the
 * server-trusted price captured when the item was added, never a
 * client-supplied value.
 */
public class CartItemDTO implements Serializable {

    private int foodItemId;
    private String name;
    private BigDecimal unitPrice;
    private int quantity;
    private String specialInstructions;
    private String imagePath;

    public CartItemDTO() {
    }

    public CartItemDTO(int foodItemId, String name, BigDecimal unitPrice, int quantity,
                        String specialInstructions, String imagePath) {
        this.foodItemId = foodItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
        this.imagePath = imagePath;
    }

    public int getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(int foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
