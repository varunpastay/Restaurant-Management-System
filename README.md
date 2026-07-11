# Restaurant QR Ordering & Management System

A production-grade, multi-restaurant-sellable QR ordering and back-of-house
management system built entirely on **Java, Servlets, JSP, JDBC, MySQL,
HTML5, CSS3, JavaScript and Bootstrap 5** — no Spring, no Hibernate/JPA, no
React/Angular, no Node.js, no Firebase.

A customer scans the QR code on their table, the menu opens instantly in
their browser — no login, no app install — and the order flows straight
through to the kitchen and the billing counter. Every restaurant-specific
detail (branding, menu, prices, taxes, tables, staff) is configured from the
Admin Dashboard at runtime; nothing requires touching source code.

Four surfaces, one shared MySQL database:

- **Customer** (no login) — browse menu, cart, place order, track status live
- **Kitchen** — auto-refreshing order queue, one-click status advance
- **Counter** — billing queue, GST/tax/service-charge breakdown, payments, PDF invoices
- **Admin** (secure login) — settings, menu/category/table/staff CRUD, QR generation, sales reports, backup & restore

See [docs/README.md](docs/README.md) for full setup instructions (MySQL
schema, `db.properties` configuration, build/deploy steps, first-run admin
password setup) and [docs/ER-DIAGRAM.md](docs/ER-DIAGRAM.md) for the database
design.

## Quick start (Windows dev machine)

```powershell
cd RestaurantOrderingSystem
mvn -B -q package -DskipTests
.\run.ps1
```

`run.ps1` builds, deploys, and starts Tomcat, then prints the customer
menu / staff login / admin login URLs. See `docs/README.md` for the one-time
MySQL schema + `db.properties` setup this depends on.

## Tech stack

Java 17 · Jakarta EE 10 (Servlet 6.0 / JSP 3.1 / JSTL 3.0) · Apache Tomcat
10.1+ · MySQL 8.0+ · plain JDBC (hand-written DAO/DTO, no ORM) · Bootstrap 5 ·
vanilla JavaScript/AJAX · Maven (WAR packaging). Narrow-purpose libraries:
MySQL Connector/J, ZXing (QR codes), Apache POI (Excel export), OpenPDF (PDF
export).
