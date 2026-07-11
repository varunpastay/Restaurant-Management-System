package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** Aggregated order count/revenue for one hour-of-day (0-23) within a report's date range - powers the "peak hours" view. */
public class HourlySalesDTO implements Serializable {

    private int hour;
    private int orderCount;
    private BigDecimal revenue;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
