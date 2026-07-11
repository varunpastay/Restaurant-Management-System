package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** One row of an aggregated sales report: how much of one food item sold within a date range. */
public class FoodSalesDTO implements Serializable {

    private int foodItemId;
    private String name;
    private int totalQuantity;
    private BigDecimal totalRevenue;

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

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
