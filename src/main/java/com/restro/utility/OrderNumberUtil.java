package com.restro.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Formats a human-friendly, date-stamped order number from an already
 * globally-unique database order_id (e.g. ORD-20260703-0007). Using the
 * AUTO_INCREMENT id as the uniqueness source (rather than a per-day counter
 * query) avoids any race condition between concurrently placed orders.
 */
public final class OrderNumberUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OrderNumberUtil() {
    }

    public static String format(LocalDate date, long orderId) {
        return "ORD-" + date.format(DATE_FORMAT) + "-" + String.format("%04d", orderId);
    }
}
