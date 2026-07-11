# Restaurant QR Ordering & Management System

A production-grade, multi-restaurant-sellable QR ordering and back-of-house
management system built entirely on **Java, Servlets, JSP, JDBC, MySQL,
HTML5, CSS3, JavaScript and Bootstrap 5** — no Spring, no Hibernate/JPA, no
React/Angular, no Node.js, no Firebase. Every restaurant-specific detail
(branding, menu, prices, taxes, tables, staff) is configured from the Admin
Dashboard at runtime; nothing requires touching source code or redeploying.

A customer scans the QR code on their table, the menu opens instantly in
their browser — no login, no app install, no signup — and the order flows
straight through to the kitchen and the billing counter.

---

## 1. Feature Summary

**Customer** (no login): scan QR → browse menu (search, category filters,
veg/non-veg, spice level, recommended/bestseller badges, images) → add to
cart (quantity, per-item notes, order-level note) → place order → live
order-status tracking page.

**Kitchen**: AJAX auto-refreshing order queue, new-order chime, one-click
status advance (Pending → Accepted → Preparing → Ready → Served), sees
every item, quantity, and note per order.

**Counter**: AJAX auto-refreshing billing queue (orders served, not yet
paid), full bill view with GST/tax/service-charge/discount breakdown, mark
paid (Cash/Card/UPI/Other, auto invoice numbering), cancel order, print
bill, download/reprint a PDF invoice, look up any historical order by
number.

**Admin** (secure login): Dashboard, full Restaurant Settings (name, logo,
banner, address, phone, email, GSTIN, currency, multiple named taxes,
service charge %, theme color, dark-mode default, opening/closing hours,
open/closed toggle), Category CRUD, Food Item CRUD (multi-image upload,
offer price, prep time, veg/non-veg/egg, spice level, recommended/
bestseller, availability toggle), Table CRUD + QR generate/download/print,
Staff CRUD (kitchen/counter accounts), Sales Reports (today/week/month/year
revenue, top/least sellers, peak hours, average order value, daily revenue
trend, Excel + PDF export), Database Backup & Restore.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 17, Jakarta EE 10 (Servlet 6.0 / JSP 3.1 / JSTL 3.0) |
| Server | Apache Tomcat 10.1+ |
| Database | MySQL 8.0+ |
| Persistence | Plain JDBC, hand-written DAO/DTO pattern (no ORM) |
| Frontend | JSP + JSTL (server-rendered), Bootstrap 5, vanilla JavaScript + `fetch()`, AJAX |
| Build | Maven (WAR packaging) |

Three narrow-purpose libraries are used beyond the JDK/servlet APIs, each
solving something the JDK genuinely cannot do alone — **none are
application frameworks**:

- **MySQL Connector/J** — the JDBC driver (required for any JDBC+MySQL app)
- **ZXing** — QR code image generation
- **Apache POI** — Excel (.xlsx) sales report export
- **OpenPDF** — PDF invoice and sales report export
- **JSTL** — JSP Standard Tag Library (display-only tags, no business logic)

Everything else — the connection pool, password hashing, session
management, the AJAX JSON responses — is hand-written against the JDK and
Servlet API directly.

---

## 3. Architecture

Strict MVC. JSPs contain only JSTL/EL for display — no scriptlets, no
business logic. Controllers (servlets) call a thin service layer or the DAO
layer directly and forward request-scoped DTOs to a JSP. All SQL lives in
`daoimpl` behind `PreparedStatement`.

```
src/main/java/com/restro/
├── dto/            Plain data-transfer objects + shared enums (OrderStatus, FoodType, ...)
├── dao/             *Dao interfaces
├── daoimpl/         *DaoImpl - the only place raw SQL is written (JDBC, PreparedStatement only)
├── service/         Thin orchestration above DAOs (order totals math, billing idempotency)
├── controller/
│   ├── customer/    MenuServlet, CartServlet, OrderServlet, OrderTrackServlet, ...
│   ├── kitchen/     KitchenDashboardServlet, KitchenOrdersApiServlet, KitchenStatusUpdateServlet
│   ├── counter/     CounterDashboardServlet, BillServlet, MarkPaidServlet, CancelOrderServlet, ...
│   ├── admin/       AuthN/Settings/Category/FoodItem/Table/QR/Staff/Report/Backup servlets
│   ├── staff/       Shared kitchen+counter login/logout
│   └── common/      ImageServingServlet (streams uploaded files from outside the WAR)
├── filters/         EncodingFilter, ExceptionHandlingFilter, AdminAuthFilter, StaffAuthFilter
├── listeners/        AppContextListener (startup/shutdown wiring)
└── utility/         DBConnectionUtil, PasswordUtil, FileUploadUtil, QRCodeUtil, PdfInvoiceUtil,
                       ExcelReportUtil, DatabaseBackupUtil, JsonUtil/JsonResponseUtil, ...
```

122 Java classes, 22 JSPs/fragments, one SQL schema.

### Notable design decisions

- **Self-contained JDBC connection pool** (`DBConnectionUtil`) — no external
  pooling library, no container-level JNDI `DataSource` configuration
  required. Pooled connections are handed out via a JDK dynamic proxy so
  ordinary `connection.close()` in DAO code returns the connection to the
  pool instead of closing the socket. Deploy the WAR to any plain Servlet
  container with nothing more than `db.properties`.
- **Single-tenant per deployment**: each restaurant runs its own instance
  and database (matching "sold to multiple restaurants" as *each restaurant
  buys and runs a copy*). The schema still carries an explicit
  `restaurant_id` FK on every top-level table, so multi-branch/multi-tenant
  support later is a `WHERE`-clause change in the DAO layer, not a schema
  rewrite.
- **Order is a transactional aggregate root**: placing an order, advancing
  its status, and cancelling it each open one connection, write to
  `orders` + `order_item` + `order_status_history` together, and
  commit/rollback as a unit.
- **Money is snapshotted**: `order_item` stores the food name and price
  *as they were* at order time, so a later menu edit never rewrites
  historical bills.
- **Race-free order/invoice numbers**: `ORD-20260703-0007` /
  `INV-20260703-0007` are derived from the already-unique database
  `order_id`/`payment_id` *after* insert, not from a separate per-day
  counter — so concurrent orders from different tables can never collide.
- **Billing is idempotent**: clicking "Mark Paid" twice never creates a
  second payment row (`payment.order_id` is `UNIQUE`; `BillingService`
  checks for an existing payment before inserting).

---

## 4. Prerequisites

- JDK 17+
- Apache Maven 3.6+
- MySQL 8.0+
- Apache Tomcat 10.1+ (Jakarta EE namespace — **not** Tomcat 9, which uses
  the old `javax.*` packages)

---

## 5. Setup

### 5.1 Create the database

```sh
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed-data.sql
```

`schema.sql` creates the `restaurant_db` database and all 15 tables.
`seed-data.sql` loads one demo restaurant ("Spice Route Bistro"), 6
categories, 17 food items, 6 tables with QR tokens, and a handful of
historical orders so the Sales Reports screen has real data to show
immediately.

### 5.2 Configure the deployment

Edit `src/main/resources/db.properties` — the **only** file a deploying
integrator needs to touch to point the app at a restaurant's MySQL
instance:

```properties
db.url=jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=CHANGE_ME
```

Edit `src/main/resources/app.properties` for server-level settings:

```properties
# Where uploaded images (logos, food photos, QR codes) are stored.
# Kept OUTSIDE the WAR on purpose so redeploys never wipe restaurant content.
upload.dir=E:/restaurant-uploads

# The public URL this app is reachable at - baked into every printed QR code.
app.base.url=http://your-domain-or-ip:8080/restaurant-ordering-system

# Only needed for Admin > Backup & Restore. Leave blank if mysqldump/mysql
# are already on the server's PATH.
mysql.bin.dir=
```

### 5.3 Set real admin/staff passwords

The seeded `admin`, `kitchen1`, and `counter1` rows ship with placeholder
password hashes that **cannot log in**. Generate real ones:

```sh
mvn compile
java -cp target/classes com.restro.utility.PasswordHashGeneratorTool
# (prompts for a password, not echoed to the terminal)
```

It prints a ready-to-run `UPDATE` statement — run it against your database
for `admin`, and again for each staff username you want to activate.

### 5.4 Build and deploy

```sh
mvn clean package
cp target/restaurant-ordering-system.war "$CATALINA_HOME/webapps/"
```

Tomcat will auto-deploy it. Watch `logs/catalina.out` for
`Startup complete.` from the app's own listener.

### 5.5 Try it

- **Customer menu**: `http://your-host:8080/restaurant-ordering-system/menu?table=1&token=<the seeded token>`
  (find real tokens: `SELECT table_no, qr_token FROM restaurant_table;`)
- **Admin**: `http://your-host:8080/restaurant-ordering-system/admin/login`
- **Kitchen / Counter**: `http://your-host:8080/restaurant-ordering-system/staff/login`

In production, generate each table's real QR code from
**Admin → Tables & QR → Generate QR** instead of hand-building URLs — it
encodes `{app.base.url}/menu?table={tableNo}&token={qrToken}` and is
downloadable/printable straight from that screen.

---

## 6. Configuring a Restaurant (No Code Required)

Everything a restaurant owner needs is under **Admin**, reachable after
logging in at `/admin/login`:

1. **Restaurant Settings** — name, logo, banner, address, contact info,
   GSTIN, currency, taxes (add as many named tax lines as needed — CGST/
   SGST/GST/VAT/...), service charge %, theme color (customer menu +
   branded pages repaint instantly), dark mode default, opening/closing
   hours, and the **Open/Closed** toggle that blocks new orders when off.
2. **Categories** — unlimited, with display order, image, active toggle.
3. **Food Items** — unlimited, with category, price + optional offer
   price, description, ingredients, prep time, veg/non-veg/egg, spice
   level, multiple photos (first upload becomes the primary/menu-card
   photo; switch it any time), recommended/bestseller badges, and an
   availability toggle that instantly hides/shows the item on the live
   customer menu.
4. **Tables & QR** — add tables, generate/regenerate/download/print each
   one's QR code.
5. **Staff** — create kitchen/counter logins, assign roles, reset
   passwords, deactivate accounts.
6. **Sales Reports** — switch between Today/Week/Month/Year, export the
   current view to Excel or PDF.
7. **Backup & Restore** — download a full database backup at any time;
   restoring requires typing a literal confirmation phrase since it
   overwrites all current data.

---

## 7. Database

See [`ER-DIAGRAM.md`](ER-DIAGRAM.md) for the full entity-relationship
diagram and table-by-table notes. Schema source: [`sql/schema.sql`](../sql/schema.sql).

15 tables: `restaurant`, `admin`, `staff`, `settings`, `category`,
`food_item`, `food_image`, `restaurant_table`, `qr_code`, `tax`,
`discount`, `orders`, `order_item`, `order_status_history`, `payment`.

The schema is deliberately **inventory-ready**: a future `inventory_item` +
`food_item_ingredient` join table can hang off `food_item_id` without
altering a single existing column.

---

## 8. Security Notes

- Every SQL query is parameterized (`PreparedStatement`) — no string-built
  SQL anywhere.
- Passwords are hashed with PBKDF2WithHmacSHA256 (120,000 iterations,
  per-password random salt) — JDK-only, no external hashing library.
- Every JSP escapes user/admin-supplied text with JSTL's `fn:escapeXml()`
  before rendering it — including customer-supplied order notes/special
  instructions, which is the highest-risk data flow in the system (an
  unauthenticated customer's text ends up on an authenticated staff
  member's screen).
- Uploaded images are content-validated (decoded via `ImageIO` for JPEG/
  PNG, magic-byte checked for WebP) — a script file renamed to `.jpg` is
  rejected, not just filtered by extension.
- Admin and staff sessions regenerate their session ID on login (session
  fixation protection) and use `HttpOnly` cookies.
- Admin (`/admin/*`) and staff (`/kitchen/*`, `/counter/*`) are separate
  authentication domains with separate filters — an admin session cannot
  reach kitchen/counter screens and vice versa, and a kitchen login cannot
  reach counter screens.
- **Known trade-off**: there is no token-based CSRF protection. Every
  state-changing action is POST-only (no mutating `GET` handlers exist),
  and the session cookie has no explicit `SameSite` override, so it
  inherits modern browsers' default of `SameSite=Lax`, which blocks the
  cookie on cross-site POST submissions — a meaningful baseline, but not
  as strong as per-form CSRF tokens. Adding those is a reasonable next
  hardening step for a v2 (see §9).

---

## 9. Future Enhancements (Schema/Architecture Already Accommodates)

Online payments (UPI/Card), customer feedback/ratings, loyalty points,
coupon codes (the `discount` table already models this), multiple
restaurant branches (the `restaurant_id` FK design), a dedicated Kitchen
Display System, native app integration, WhatsApp/SMS notifications, full
role-based access control, inventory management, multi-language support,
per-form CSRF tokens.

---

## 10. Default Demo Credentials

Only valid **after** running the `PasswordHashGeneratorTool` step above and
applying the printed `UPDATE` with your own chosen passwords — the seeded
rows have no working password out of the box, by design.

| Role | Username |
|---|---|
| Admin | `admin` |
| Kitchen | `kitchen1` |
| Counter | `counter1` |

**Change or remove these accounts before going live.**
