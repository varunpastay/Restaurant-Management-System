# Entity-Relationship Diagram

Source of truth: [`sql/schema.sql`](../sql/schema.sql). This document is a
readable companion to it, not a replacement.

```mermaid
erDiagram
    RESTAURANT ||--o{ ADMIN : has
    RESTAURANT ||--o{ STAFF : employs
    RESTAURANT ||--o{ SETTINGS : configures
    RESTAURANT ||--o{ CATEGORY : offers
    RESTAURANT ||--o{ FOOD_ITEM : sells
    RESTAURANT ||--o{ RESTAURANT_TABLE : has
    RESTAURANT ||--o{ TAX : defines
    RESTAURANT ||--o{ DISCOUNT : defines
    RESTAURANT ||--o{ ORDERS : receives

    CATEGORY ||--o{ FOOD_ITEM : contains
    FOOD_ITEM ||--o{ FOOD_IMAGE : has
    FOOD_ITEM ||--o{ ORDER_ITEM : "ordered as"

    RESTAURANT_TABLE ||--o{ QR_CODE : "printed as"
    RESTAURANT_TABLE ||--o{ ORDERS : seats

    ORDERS ||--|{ ORDER_ITEM : contains
    ORDERS ||--o{ ORDER_STATUS_HISTORY : "transitions through"
    ORDERS ||--o| PAYMENT : "settled by"
    ORDERS }o--o| DISCOUNT : "may apply"
    STAFF ||--o{ ORDER_STATUS_HISTORY : "changed by"

    RESTAURANT {
        int restaurant_id PK
        varchar name
        varchar logo_path
        varchar banner_path
        varchar address
        varchar phone
        varchar email
        varchar gstin
        varchar currency_code
        varchar currency_symbol
        decimal service_charge_percent
        varchar theme_color
        boolean dark_mode_default
        time opening_time
        time closing_time
        boolean is_open
    }
    ADMIN {
        int admin_id PK
        int restaurant_id FK
        varchar email UK
        varchar password_hash
        varchar password_salt
        varchar full_name
        boolean is_active
    }
    STAFF {
        int staff_id PK
        int restaurant_id FK
        varchar email UK
        varchar password_hash
        varchar password_salt
        varchar full_name
        enum role "KITCHEN | COUNTER"
        boolean is_active
    }
    SETTINGS {
        int setting_id PK
        int restaurant_id FK
        varchar setting_key
        varchar setting_value
    }
    CATEGORY {
        int category_id PK
        int restaurant_id FK
        varchar name
        int display_order
        varchar image_path
        boolean is_active
    }
    FOOD_ITEM {
        int food_item_id PK
        int restaurant_id FK
        int category_id FK
        varchar name
        text description
        text ingredients
        decimal price
        decimal offer_price
        int prep_time_minutes
        enum food_type "VEG | NON_VEG | EGG"
        enum spice_level "MILD..EXTRA_HOT"
        boolean is_available
        boolean is_recommended
        boolean is_bestseller
    }
    FOOD_IMAGE {
        int food_image_id PK
        int food_item_id FK
        varchar image_path
        boolean is_primary
    }
    RESTAURANT_TABLE {
        int table_id PK
        int restaurant_id FK
        varchar table_no
        int capacity
        varchar qr_token UK
        boolean is_active
    }
    QR_CODE {
        int qr_code_id PK
        int table_id FK
        varchar image_path
        varchar target_url
        datetime generated_at
    }
    TAX {
        int tax_id PK
        int restaurant_id FK
        varchar name
        decimal percent
        boolean is_active
    }
    DISCOUNT {
        int discount_id PK
        int restaurant_id FK
        varchar code UK
        enum discount_type "PERCENT | FLAT"
        decimal value
        boolean is_active
    }
    ORDERS {
        int order_id PK
        int restaurant_id FK
        varchar order_no UK
        int table_id FK
        enum status "PENDING..CANCELLED"
        decimal subtotal
        decimal tax_amount
        decimal service_charge_amount
        decimal discount_amount
        decimal grand_total
        int discount_id FK
        varchar customer_note
    }
    ORDER_ITEM {
        int order_item_id PK
        int order_id FK
        int food_item_id FK
        varchar food_name_snapshot
        decimal unit_price
        int quantity
        varchar special_instructions
        decimal line_total
    }
    ORDER_STATUS_HISTORY {
        int history_id PK
        int order_id FK
        enum status
        datetime changed_at
        int changed_by_staff_id FK
    }
    PAYMENT {
        int payment_id PK
        int order_id FK "UNIQUE"
        varchar invoice_no UK
        decimal amount
        enum method "CASH|CARD|UPI|OTHER"
        enum payment_status "PAID|REFUNDED"
        datetime paid_at
    }
```

## Notes on specific design choices

- **`restaurant_id` on every top-level table, even in a single-tenant
  deployment.** Strict child rows (`food_image`, `order_item`,
  `order_status_history`, `payment`, `qr_code`) don't repeat it - they
  reach it by joining through their parent. This keeps the schema
  normalized today while making a future multi-branch/multi-tenant feature
  a `WHERE`-clause change, not a migration.
- **`tax` is a table, not a single `restaurant.tax_percent` column.** A
  restaurant can configure any number of named tax lines (CGST + SGST,
  a single GST, VAT, ...) and the bill/invoice renders them all by name.
- **`order_item.food_name_snapshot` and `unit_price` are copies**, taken at
  order time. Renaming, repricing, or deleting a food item later never
  rewrites a historical bill.
- **`payment` only gets a row once an order is actually settled.** No row
  for an order means "unpaid" - there's no separate `payment_status` flag
  to keep in sync on `orders` itself.
- **`order_status_history` is append-only.** It powers both the
  customer-facing tracking timeline and the admin-facing peak-hours /
  order-trend reports without recomputing anything from `orders.updated_at`
  alone.
- **`restaurant_table.qr_token` is a random opaque string**, not the
  numeric `table_id`, and is what the no-login customer flow actually
  trusts to resolve `?table=&token=` to a real table - the numeric id in
  the URL is there only for human readability.
- **Inventory-ready by omission**: no inventory tables exist yet, but
  `food_item_id` is a stable FK target a future `inventory_item` /
  `food_item_ingredient` table can reference without touching any existing
  column.
