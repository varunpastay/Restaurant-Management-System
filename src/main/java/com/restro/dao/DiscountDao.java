package com.restro.dao;

import com.restro.dto.DiscountDTO;

import java.sql.SQLException;
import java.util.List;

public interface DiscountDao {

    List<DiscountDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    DiscountDTO findById(int discountId) throws SQLException;

    /** Only matches an active, currently-valid (within valid_from/valid_to) code. */
    DiscountDTO findActiveByCode(int restaurantId, String code) throws SQLException;

    int insert(DiscountDTO discount) throws SQLException;

    boolean update(DiscountDTO discount) throws SQLException;

    boolean delete(int discountId) throws SQLException;
}
