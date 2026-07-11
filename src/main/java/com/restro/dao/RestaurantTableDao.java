package com.restro.dao;

import com.restro.dto.RestaurantTableDTO;

import java.sql.SQLException;
import java.util.List;

public interface RestaurantTableDao {

    List<RestaurantTableDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    RestaurantTableDTO findById(int tableId) throws SQLException;

    /** Looks a table up by the opaque token embedded in its QR URL - this is how the no-login customer menu resolves ?table=&token= to a real table. */
    RestaurantTableDTO findByToken(String qrToken) throws SQLException;

    boolean existsByTableNo(int restaurantId, String tableNo, Integer excludeTableId) throws SQLException;

    int insert(RestaurantTableDTO table) throws SQLException;

    boolean update(RestaurantTableDTO table) throws SQLException;

    boolean delete(int tableId) throws SQLException;
}
