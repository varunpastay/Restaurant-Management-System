package com.restro.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The session-scoped shopping cart for one table's visit. Stored directly as
 * an HttpSession attribute (see CartServlet) - scoped to a single table via
 * {@code tableId} so a stale session can't silently place an order against
 * the wrong table if a browser somehow carries a cookie across QR scans.
 */
public class CartDTO implements Serializable {

    private Integer tableId;
    private final List<CartItemDTO> items = new ArrayList<>();

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItemDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<CartItemDTO> find(int foodItemId) {
        return items.stream().filter(i -> i.getFoodItemId() == foodItemId).findFirst();
    }

    /** Adds a new line or increments the existing one's quantity by {@code quantity}. */
    public void addOrIncrement(FoodItemDTO food, int quantity, String specialInstructions) {
        Optional<CartItemDTO> existing = find(food.getFoodItemId());
        if (existing.isPresent()) {
            CartItemDTO item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (specialInstructions != null && !specialInstructions.isBlank()) {
                item.setSpecialInstructions(specialInstructions);
            }
        } else {
            items.add(new CartItemDTO(food.getFoodItemId(), food.getName(), food.getEffectivePrice(),
                    quantity, specialInstructions, food.getPrimaryImagePath()));
        }
    }

    /** Sets an absolute quantity; a value &lt;= 0 removes the line. */
    public void updateQuantity(int foodItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(foodItemId);
            return;
        }
        find(foodItemId).ifPresent(item -> item.setQuantity(quantity));
    }

    public void updateInstructions(int foodItemId, String specialInstructions) {
        find(foodItemId).ifPresent(item -> item.setSpecialInstructions(specialInstructions));
    }

    public void removeItem(int foodItemId) {
        items.removeIf(i -> i.getFoodItemId() == foodItemId);
    }

    public void clear() {
        items.clear();
    }

    public int quantityOf(int foodItemId) {
        return find(foodItemId).map(CartItemDTO::getQuantity).orElse(0);
    }
}
