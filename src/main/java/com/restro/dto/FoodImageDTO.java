package com.restro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FoodImageDTO implements Serializable {

    private int foodImageId;
    private int foodItemId;
    private String imagePath;
    private boolean primary;
    private int displayOrder;
    private LocalDateTime createdAt;

    public int getFoodImageId() {
        return foodImageId;
    }

    public void setFoodImageId(int foodImageId) {
        this.foodImageId = foodImageId;
    }

    public int getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(int foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
