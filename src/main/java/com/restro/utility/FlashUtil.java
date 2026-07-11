package com.restro.utility;

import jakarta.servlet.http.HttpSession;

/**
 * Minimal POST/redirect/GET "flash message" support: a validation error or
 * success message is stashed on the session right before a redirect, then
 * read and cleared on the very next request. Used by every admin CRUD
 * screen (Category/FoodItem/Table/Staff) so a redirect-after-POST doesn't
 * lose the user's feedback message, without resorting to forwarding (which
 * would let a page refresh resubmit the form).
 */
public final class FlashUtil {

    private static final String ERROR_KEY = "flashError";
    private static final String SUCCESS_KEY = "flashSuccess";

    private FlashUtil() {
    }

    public static void setError(HttpSession session, String message) {
        session.setAttribute(ERROR_KEY, message);
    }

    public static void setSuccess(HttpSession session, String message) {
        session.setAttribute(SUCCESS_KEY, message);
    }

    public static String consumeError(HttpSession session) {
        Object value = session.getAttribute(ERROR_KEY);
        session.removeAttribute(ERROR_KEY);
        return value != null ? value.toString() : null;
    }

    public static String consumeSuccess(HttpSession session) {
        Object value = session.getAttribute(SUCCESS_KEY);
        session.removeAttribute(SUCCESS_KEY);
        return value != null ? value.toString() : null;
    }
}
