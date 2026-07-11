package com.restro.dao;

import com.restro.dto.FoodImageDTO;

import java.sql.SQLException;
import java.util.List;

public interface FoodImageDao {

    List<FoodImageDTO> findByFoodItem(int foodItemId) throws SQLException;

    FoodImageDTO findPrimaryByFoodItem(int foodItemId) throws SQLException;

    int insert(FoodImageDTO image) throws SQLException;

    boolean delete(int foodImageId) throws SQLException;

    /** Clears is_primary on every other image for the same food item, then sets it on this one. */
    boolean setPrimary(int foodItemId, int foodImageId) throws SQLException;
}
