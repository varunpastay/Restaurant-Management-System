package com.restro.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formats a human-friendly, date-stamped invoice number from an already globally-unique database payment_id (e.g. INV-20260703-0007). */
public final class InvoiceNumberUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private InvoiceNumberUtil() {
    }

    public static String format(LocalDate date, long paymentId) {
        return "INV-" + date.format(DATE_FORMAT) + "-" + String.format("%04d", paymentId);
    }
}
