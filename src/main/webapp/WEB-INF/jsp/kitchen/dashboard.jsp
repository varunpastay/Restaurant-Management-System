<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Kitchen Dashboard - ${fn:escapeXml(restaurant.name)}</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="pb-4">

<header class="restaurant-header py-2">
    <div class="container d-flex align-items-center justify-content-between">
        <div>
            <h5 class="mb-0 fw-bold"><i class="bi bi-fire brand-text"></i> Kitchen Dashboard</h5>
            <small class="text-muted-brand">${fn:escapeXml(restaurant.name)} &middot; ${fn:escapeXml(staffName)}</small>
        </div>
        <div class="d-flex align-items-center gap-2">
            <span id="liveIndicator" class="badge bg-success"><i class="bi bi-broadcast"></i> Live</span>
            <a href="${pageContext.request.contextPath}/staff/logout" class="btn btn-outline-brand btn-sm">Logout</a>
        </div>
    </div>
</header>

<div class="container py-3">
    <div id="emptyQueueMessage" class="text-center text-muted-brand py-5 d-none">
        <i class="bi bi-emoji-smile empty-state-icon"></i>
        <p class="mt-2">No active orders. All caught up!</p>
    </div>
    <div id="ordersGrid" class="row g-3"></div>
</div>

<script>
    window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
    window.REFRESH_INTERVAL_MS = ${refreshIntervalSeconds} * 1000;
</script>
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/notification-sound.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/kitchen.js"></script>
</body>
</html>
