<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Restaurant QR Ordering &amp; Management System</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>

<header class="restaurant-header py-3">
    <div class="container d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center gap-2">
            <i class="bi bi-shop brand-text fs-3"></i>
            <div>
                <h6 class="mb-0 fw-bold">Restaurant QR Ordering &amp; Management System</h6>
                <small class="text-muted-brand">Java &middot; Servlets &middot; JSP &middot; JDBC &middot; MySQL &middot; Bootstrap 5</small>
            </div>
        </div>
        <a href="https://github.com/varunpastay/Restaurant-Management-System" target="_blank" rel="noopener"
           class="btn btn-outline-brand btn-sm">
            <i class="bi bi-github"></i> <span class="d-none d-sm-inline">Source</span>
        </a>
    </div>
</header>

<div class="d-flex align-items-center justify-content-center text-center"
     style="background: radial-gradient(circle at 50% 0%, color-mix(in srgb, var(--brand) 22%, var(--surface-bg)) 0%, var(--surface-bg) 60%); min-height: 220px;">
    <div class="container animate-in">
        <h2 class="fw-bold mb-2">A no-login QR ordering system, end to end</h2>
        <p class="text-muted-brand mb-0" style="max-width: 640px; margin: 0 auto;">
            A customer scans a table QR code and orders straight from their phone - no app, no signup.
            The order flows live to the kitchen, then the billing counter, all from one admin-configurable
            dashboard. Explore all four roles below with the demo logins provided.
        </p>
    </div>
</div>

<div class="container py-4">
    <div class="row g-3">

        <div class="col-md-4">
            <div class="card h-100 animate-in" style="animation-delay:0.05s">
                <div class="card-body d-flex flex-column">
                    <div class="stat-icon mb-3"><i class="bi bi-qr-code-scan"></i></div>
                    <h5 class="fw-bold mb-1">Customer</h5>
                    <p class="text-muted-brand small flex-grow-1">
                        No login. Scan the table QR, browse the menu, add items to cart, place an order,
                        and track its status live from Pending through Served.
                    </p>
                    <a href="${pageContext.request.contextPath}/menu?table=1&amp;token=a1e6f9c2b3d84e0f9a1c2b3d4e5f6071"
                       class="btn btn-brand w-100 mb-2">
                        <i class="bi bi-box-arrow-up-right"></i> Open customer menu
                    </a>
                    <div class="small text-muted-brand text-center">No login required</div>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card h-100 animate-in" style="animation-delay:0.1s">
                <div class="card-body d-flex flex-column">
                    <div class="stat-icon mb-3"><i class="bi bi-fire"></i></div>
                    <h5 class="fw-bold mb-1">Kitchen &amp; Counter Staff</h5>
                    <p class="text-muted-brand small flex-grow-1">
                        One shared login screen; the account's role decides the dashboard. Kitchen advances
                        orders through their lifecycle; Counter bills served orders and prints/downloads invoices.
                    </p>
                    <a href="${pageContext.request.contextPath}/staff/login" class="btn btn-brand w-100 mb-2">
                        <i class="bi bi-box-arrow-up-right"></i> Open staff login
                    </a>
                    <div class="small text-center">
                        <div class="text-muted-brand">Kitchen: <code>kitchen@spicerouteBistro.example</code></div>
                        <div class="text-muted-brand">Counter: <code>counter@spicerouteBistro.example</code></div>
                        <div class="text-muted-brand">Password: <code>Qa$Pass123</code></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card h-100 animate-in" style="animation-delay:0.15s">
                <div class="card-body d-flex flex-column">
                    <div class="stat-icon mb-3"><i class="bi bi-speedometer2"></i></div>
                    <h5 class="fw-bold mb-1">Admin</h5>
                    <p class="text-muted-brand small flex-grow-1">
                        Full no-code control: branding, menu, categories, tables &amp; QR generation, staff
                        accounts, taxes, sales reports (Excel/PDF export), and database backup/restore.
                    </p>
                    <a href="${pageContext.request.contextPath}/admin/login" class="btn btn-brand w-100 mb-2">
                        <i class="bi bi-box-arrow-up-right"></i> Open admin login
                    </a>
                    <div class="small text-center">
                        <div class="text-muted-brand">Email: <code>owner@spicerouteBistro.example</code></div>
                        <div class="text-muted-brand">Password: <code>Qa$Pass123</code></div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div class="text-center text-muted-brand small mt-4">
        Sample data only - this is a live demo deployment, not a real restaurant.
    </div>
</div>

</body>
</html>
