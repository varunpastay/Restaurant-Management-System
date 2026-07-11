<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Admin Dashboard - ${fn:escapeXml(restaurant.name)}</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-3 p-md-4">
        <h4 class="fw-bold mb-1 animate-in">Welcome back${not empty sessionScope.adminFullName ? ', ' : ''}${fn:escapeXml(sessionScope.adminFullName)}</h4>
        <p class="text-muted-brand mb-4 animate-in" style="animation-delay:0.03s">
            <span class="status-dot ${restaurant.open ? 'open' : 'closed'}"></span>
            ${fn:escapeXml(restaurant.name)} &middot; ${restaurant.open ? 'Currently Open' : 'Currently Closed'}
        </p>

        <div class="row g-3">
            <div class="col-sm-6 col-lg-4">
                <div class="card stat-card h-100 animate-in" style="animation-delay:0.06s">
                    <div class="card-body">
                        <div class="stat-icon mb-2"><i class="bi bi-fire"></i></div>
                        <div class="small text-muted-brand">Active Kitchen Orders</div>
                        <div class="fs-2 fw-bold">${activeOrderCount}</div>
                        <a href="${pageContext.request.contextPath}/kitchen/dashboard" class="small">View kitchen queue &rarr;</a>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-4">
                <div class="card stat-card h-100 animate-in" style="animation-delay:0.1s">
                    <div class="card-body">
                        <div class="stat-icon mb-2"><i class="bi bi-receipt-cutoff"></i></div>
                        <div class="small text-muted-brand">Awaiting Billing</div>
                        <div class="fs-2 fw-bold">${awaitingBillingCount}</div>
                        <a href="${pageContext.request.contextPath}/counter/dashboard" class="small">View billing queue &rarr;</a>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-4">
                <div class="card stat-card h-100 animate-in" style="animation-delay:0.14s">
                    <div class="card-body">
                        <div class="stat-icon mb-2"><i class="bi bi-gear-wide-connected"></i></div>
                        <div class="small text-muted-brand mb-1">Quick Setup</div>
                        <a href="${pageContext.request.contextPath}/admin/settings" class="d-block mt-2"><i class="bi bi-arrow-right-short"></i> Configure restaurant settings</a>
                        <a href="${pageContext.request.contextPath}/admin/tables" class="d-block mt-1"><i class="bi bi-arrow-right-short"></i> Manage tables &amp; QR codes</a>
                        <a href="${pageContext.request.contextPath}/admin/food" class="d-block mt-1"><i class="bi bi-arrow-right-short"></i> Manage menu &amp; food items</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
