package com.restro.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** One append-only row per lifecycle transition; powers the customer tracking timeline and future peak-hour/prep-duration analytics. */
public class OrderStatusHistoryDTO implements Serializable {

    private int historyId;
    private int orderId;
    private OrderStatus status;
    private LocalDateTime changedAt;
    private Integer changedByStaffId;

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public Integer getChangedByStaffId() {
        return changedByStaffId;
    }

    public void setChangedByStaffId(Integer changedByStaffId) {
        this.changedByStaffId = changedByStaffId;
    }
}
