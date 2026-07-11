package com.restro.dto;

/** A payment row is only ever created once an order is settled, so PAID is the natural default; REFUNDED covers a later reversal. */
public enum PaymentStatus {
    PAID,
    REFUNDED
}
