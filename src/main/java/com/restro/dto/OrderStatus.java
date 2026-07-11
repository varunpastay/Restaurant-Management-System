package com.restro.dto;

/** Order lifecycle: Pending -> Accepted -> Preparing -> Ready -> Served -> Completed, or Cancelled at any point before Served. */
public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    SERVED,
    COMPLETED,
    CANCELLED
}
