package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Aggregated order count/revenue for one calendar day within a report's date range - powers the revenue graph / order trends view. */
public class DailySalesDTO implements Serializable {

    private LocalDate date;
    private int orderCount;
    private BigDecimal revenue;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
