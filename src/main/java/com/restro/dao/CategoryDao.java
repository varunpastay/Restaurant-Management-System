package com.restro.dao;

import com.restro.dto.CategoryDTO;

import java.sql.SQLException;
import java.util.List;

public interface CategoryDao {

    /** All categories (active + inactive), ordered by display_order - for the Admin catalog screen. */
    List<CategoryDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    /** Only active categories, ordered by display_order - for the customer-facing menu. */
    List<CategoryDTO> findActiveByRestaurant(int restaurantId) throws SQLException;

    CategoryDTO findById(int categoryId) throws SQLException;

    boolean existsByName(int restaurantId, String name, Integer excludeCategoryId) throws SQLException;

    int insert(CategoryDTO category) throws SQLException;

    boolean update(CategoryDTO category) throws SQLException;

    boolean delete(int categoryId) throws SQLException;
}
