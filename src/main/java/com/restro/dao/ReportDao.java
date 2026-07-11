package com.restro.dao;

import com.restro.dto.DailySalesDTO;
import com.restro.dto.FoodSalesDTO;
import com.restro.dto.HourlySalesDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregate sales queries for the Admin Reports screen. Every method scopes
 * to {@code status = COMPLETED} orders only (an order that was never paid
 * or was cancelled isn't "sales") within the given [from, to) window.
 */
public interface ReportDao {

    BigDecimal getTotalRevenue(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException;

    int getOrderCount(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException;

    List<FoodSalesDTO> getTopSellingFoods(int restaurantId, LocalDateTime from, LocalDateTime to, int limit) throws SQLException;

    List<FoodSalesDTO> getLeastSellingFoods(int restaurantId, LocalDateTime from, LocalDateTime to, int limit) throws SQLException;

    /** Ordered by hour (0-23), only hours with at least one order. */
    List<HourlySalesDTO> getHourlySales(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException;

    /** Ordered by date ascending - drives the revenue graph / order trend view. */
    List<DailySalesDTO> getDailySales(int restaurantId, LocalDateTime from, LocalDateTime to) throws SQLException;
}
