<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Sales Reports - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
    <style>
        .bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
        .bar-label { width: 110px; font-size: 0.85rem; flex-shrink: 0; }
        .bar-track { flex-grow: 1; background: var(--surface-border); border-radius: 4px; height: 18px; overflow: hidden; }
        .bar-fill { background: var(--brand); height: 100%; border-radius: 4px; transition: width 0.5s ease; }
        .bar-value { width: 100px; font-size: 0.85rem; text-align: right; flex-shrink: 0; font-variant-numeric: tabular-nums; }
        @media (max-width: 575.98px) {
            .bar-label { width: 72px; font-size: 0.78rem; }
            .bar-value { width: 76px; font-size: 0.78rem; }
        }
    </style>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
            <h4 class="fw-bold mb-0">Sales Reports</h4>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/admin/reports/export/excel?period=${period}" class="btn btn-outline-brand btn-sm">
                    <i class="bi bi-file-earmark-excel"></i> Export Excel
                </a>
                <a href="${pageContext.request.contextPath}/admin/reports/export/pdf?period=${period}" class="btn btn-outline-brand btn-sm" target="_blank">
                    <i class="bi bi-file-earmark-pdf"></i> Export PDF
                </a>
            </div>
        </div>

        <div class="btn-group mb-4">
            <a href="?period=today" class="btn btn-sm ${period == 'today' ? 'btn-brand' : 'btn-outline-brand'}">Today</a>
            <a href="?period=week" class="btn btn-sm ${period == 'week' ? 'btn-brand' : 'btn-outline-brand'}">This Week</a>
            <a href="?period=month" class="btn btn-sm ${period == 'month' ? 'btn-brand' : 'btn-outline-brand'}">This Month</a>
            <a href="?period=year" class="btn btn-sm ${period == 'year' ? 'btn-brand' : 'btn-outline-brand'}">This Year</a>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-sm-4">
                <div class="card stat-card h-100"><div class="card-body">
                    <div class="stat-icon mb-2"><i class="bi bi-cash-stack"></i></div>
                    <div class="small text-muted-brand">Total Revenue</div>
                    <div class="fs-3 fw-bold">${restaurant.currencySymbol}${totalRevenue}</div>
                </div></div>
            </div>
            <div class="col-sm-4">
                <div class="card stat-card h-100"><div class="card-body">
                    <div class="stat-icon mb-2"><i class="bi bi-receipt"></i></div>
                    <div class="small text-muted-brand">Completed Orders</div>
                    <div class="fs-3 fw-bold">${orderCount}</div>
                </div></div>
            </div>
            <div class="col-sm-4">
                <div class="card stat-card h-100"><div class="card-body">
                    <div class="stat-icon mb-2"><i class="bi bi-graph-up-arrow"></i></div>
                    <div class="small text-muted-brand">Average Order Value</div>
                    <div class="fs-3 fw-bold">${restaurant.currencySymbol}${avgOrderValue}</div>
                </div></div>
            </div>
        </div>

        <div class="row g-3">
            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header fw-bold">Top Selling Items</div>
                    <div class="card-body">
                        <c:if test="${empty topSelling}"><p class="text-muted-brand small mb-0">No sales in this period yet.</p></c:if>
                        <c:forEach items="${topSelling}" var="f">
                            <div class="bar-row">
                                <div class="bar-label text-truncate" title="${fn:escapeXml(f.name)}">${fn:escapeXml(f.name)}</div>
                                <div class="bar-track"><div class="bar-fill" style="width:${(f.totalQuantity / maxTopSellingQty) * 100}%"></div></div>
                                <div class="bar-value">${f.totalQuantity} sold</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header fw-bold">Least Selling Items</div>
                    <div class="card-body">
                        <c:if test="${empty leastSelling}"><p class="text-muted-brand small mb-0">No sales in this period yet.</p></c:if>
                        <div class="table-responsive">
                        <table class="table table-sm mb-0">
                            <c:forEach items="${leastSelling}" var="f">
                                <tr><td>${fn:escapeXml(f.name)}</td><td class="text-end">${f.totalQuantity} sold</td></tr>
                            </c:forEach>
                        </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header fw-bold">Peak Hours (by revenue)</div>
                    <div class="card-body">
                        <c:if test="${empty hourlySales}"><p class="text-muted-brand small mb-0">No sales in this period yet.</p></c:if>
                        <c:forEach items="${hourlySales}" var="h">
                            <div class="bar-row">
                                <div class="bar-label">${h.hour}:00 - ${h.hour + 1}:00</div>
                                <div class="bar-track"><div class="bar-fill" style="width:${(h.revenue / maxHourlyRevenue) * 100}%"></div></div>
                                <div class="bar-value">${restaurant.currencySymbol}${h.revenue}</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header fw-bold">Revenue Trend / Order Trends</div>
                    <div class="card-body">
                        <c:if test="${empty dailySales}"><p class="text-muted-brand small mb-0">No sales in this period yet.</p></c:if>
                        <c:forEach items="${dailySales}" var="d">
                            <div class="bar-row">
                                <div class="bar-label">${d.date}</div>
                                <div class="bar-track"><div class="bar-fill" style="width:${(d.revenue / maxDailyRevenue) * 100}%"></div></div>
                                <div class="bar-value">${restaurant.currencySymbol}${d.revenue} (${d.orderCount})</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
