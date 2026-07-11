package com.restro.dao;

import com.restro.dto.FoodItemDTO;

import java.sql.SQLException;
import java.util.List;

public interface FoodItemDao {

    /** All items for this restaurant (incl. unavailable), joined with category name + primary image - for the Admin catalog screen. */
    List<FoodItemDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    /** Only available items for one category, for the customer-facing menu. */
    List<FoodItemDTO> findAvailableByCategory(int categoryId) throws SQLException;

    /**
     * All available items across every category, for the customer-facing menu.
     * The menu page filters/searches this set client-side in JS (instant, no
     * network round-trip, still fine at typical menu sizes) rather than
     * re-querying the server per keystroke.
     */
    List<FoodItemDTO> findAvailableByRestaurant(int restaurantId) throws SQLException;

    FoodItemDTO findById(int foodItemId) throws SQLException;

    int insert(FoodItemDTO item) throws SQLException;

    boolean update(FoodItemDTO item) throws SQLException;

    boolean delete(int foodItemId) throws SQLException;

    boolean setAvailability(int foodItemId, boolean available) throws SQLException;
}
