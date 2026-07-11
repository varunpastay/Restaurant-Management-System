<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Admin Login</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="d-flex align-items-center justify-content-center p-3" style="min-height:100vh; background: radial-gradient(circle at 50% 0%, color-mix(in srgb, var(--brand) 16%, var(--surface-bg)) 0%, var(--surface-bg) 55%);">
    <div class="card shadow-lg animate-in" style="width:100%; max-width:380px;">
        <div class="card-body p-4">
            <div class="text-center mb-4">
                <div class="mx-auto mb-2 d-inline-flex align-items-center justify-content-center rounded-circle"
                     style="width:64px; height:64px; background-color:color-mix(in srgb, var(--brand) 14%, transparent);">
                    <i class="bi bi-shop brand-text" style="font-size:1.8rem;"></i>
                </div>
                <h5 class="mt-1 mb-0 fw-bold">Restaurant Admin</h5>
                <small class="text-muted-brand">Sign in to manage your restaurant</small>
            </div>
            <c:if test="${not empty error}">
                <div class="alert alert-danger py-2 small"><i class="bi bi-exclamation-triangle"></i> ${fn:escapeXml(error)}</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/admin/login">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Username</label>
                    <input type="text" name="username" class="form-control" required autofocus autocomplete="username">
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Password</label>
                    <input type="password" name="password" class="form-control" required autocomplete="current-password">
                </div>
                <button type="submit" class="btn btn-brand w-100">
                    <i class="bi bi-box-arrow-in-right"></i> Log In
                </button>
            </form>
        </div>
    </div>
</body>
</html>
