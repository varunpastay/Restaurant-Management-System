package com.restro.dao;

import com.restro.dto.RestaurantDTO;

import java.sql.SQLException;

public interface RestaurantDao {

    /** Convenience for this single-tenant-per-deployment build: the one restaurant row that exists. */
    RestaurantDTO findFirst() throws SQLException;

    RestaurantDTO findById(int restaurantId) throws SQLException;

    boolean update(RestaurantDTO restaurant) throws SQLException;
}
