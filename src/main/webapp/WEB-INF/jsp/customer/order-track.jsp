<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="${restaurant.darkModeDefault ? 'dark' : 'light'}">
<head>
    <title>Order ${fn:escapeXml(order.orderNo)}</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="pb-4">
<div class="container py-4" style="max-width:560px;">
    <div class="d-flex justify-content-end mb-1">
        <button id="darkModeToggle" type="button" class="btn btn-outline-brand btn-sm" aria-label="Toggle dark mode">
            <i class="bi bi-moon-stars"></i>
        </button>
    </div>
    <div class="text-center mb-4 animate-in">
        <i id="statusIcon" class="bi bi-check-circle-fill brand-text" style="font-size:3.2rem;"></i>
        <h4 class="mt-2 mb-0 fw-bold">Order ${fn:escapeXml(order.orderNo)}</h4>
        <p class="text-muted-brand mb-2">Table ${fn:escapeXml(order.tableNo)}</p>
        <span id="statusBanner" class="badge rounded-pill px-3 py-2 fs-6" style="background-color:var(--brand);">${fn:escapeXml(order.status)}</span>
    </div>

    <div class="card mb-3 animate-in" style="animation-delay:0.05s">
        <div class="card-body">
            <ul class="status-timeline mb-0">
                <li data-status="PENDING"><i class="bi bi-receipt me-1"></i> Order received</li>
                <li data-status="ACCEPTED"><i class="bi bi-clipboard-check me-1"></i> Accepted by kitchen</li>
                <li data-status="PREPARING"><i class="bi bi-egg-fried me-1"></i> Preparing</li>
                <li data-status="READY"><i class="bi bi-bell me-1"></i> Ready</li>
                <li data-status="SERVED"><i class="bi bi-cup-hot me-1"></i> Served</li>
                <li data-status="COMPLETED"><i class="bi bi-check2-all me-1"></i> Completed</li>
            </ul>
        </div>
    </div>

    <div class="card mb-3 animate-in" style="animation-delay:0.1s">
        <div class="card-body">
            <h6 class="fw-bold mb-3"><i class="bi bi-bag-check brand-text"></i> Order Summary</h6>
            <c:forEach items="${order.items}" var="item">
                <div class="d-flex justify-content-between small mb-1">
                    <span>${item.quantity} &times; ${fn:escapeXml(item.foodNameSnapshot)}</span>
                    <span class="fw-semibold">${restaurant.currencySymbol}${item.lineTotal}</span>
                </div>
                <c:if test="${not empty item.specialInstructions}">
                    <div class="small text-muted-brand mb-2"><i class="bi bi-sticky"></i> ${fn:escapeXml(item.specialInstructions)}</div>
                </c:if>
            </c:forEach>
            <c:if test="${not empty order.customerNote}">
                <div class="small text-muted-brand mt-2 pt-2 border-top"><i class="bi bi-sticky"></i> Order note: ${fn:escapeXml(order.customerNote)}</div>
            </c:if>
            <hr>
            <div class="d-flex justify-content-between small mb-1"><span class="text-muted-brand">Subtotal</span><span>${restaurant.currencySymbol}${order.subtotal}</span></div>
            <div class="d-flex justify-content-between small mb-1"><span class="text-muted-brand">Tax</span><span>${restaurant.currencySymbol}${order.taxAmount}</span></div>
            <div class="d-flex justify-content-between small mb-1"><span class="text-muted-brand">Service Charge</span><span>${restaurant.currencySymbol}${order.serviceChargeAmount}</span></div>
            <c:if test="${order.discountAmount > 0}">
                <div class="d-flex justify-content-between small mb-1 text-success"><span>Discount</span><span>-${restaurant.currencySymbol}${order.discountAmount}</span></div>
            </c:if>
            <div class="d-flex justify-content-between fw-bold fs-5 mt-2 pt-2 border-top"><span>Total</span><span class="brand-text">${restaurant.currencySymbol}${order.grandTotal}</span></div>
        </div>
    </div>

    <c:if test="${not empty sessionScope.tableToken}">
        <a href="${pageContext.request.contextPath}/menu?token=${sessionScope.tableToken}" class="btn btn-outline-brand w-100">
            <i class="bi bi-arrow-left"></i> Back to Menu
        </a>
    </c:if>
</div>

<script>
    window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
    window.ORDER_NO = "${order.orderNo}";
</script>
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/order-track.js"></script>
</body>
</html>
