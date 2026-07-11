<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Staff Login</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="card shadow-sm" style="width:100%; max-width:380px;">
        <div class="card-body p-4">
            <div class="text-center mb-3">
                <i class="bi bi-shop brand-text" style="font-size:2.5rem;"></i>
                <h5 class="mt-2 mb-0">Staff Login</h5>
                <small class="text-muted-brand">Kitchen &amp; Counter access</small>
            </div>
            <c:if test="${not empty error}">
                <div class="alert alert-danger py-2 small">${fn:escapeXml(error)}</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/staff/login">
                <input type="hidden" name="redirect" value="${fn:escapeXml(redirect)}">
                <div class="mb-3">
                    <label class="form-label">Username</label>
                    <input type="text" name="username" class="form-control" required autofocus>
                </div>
                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-brand w-100">Log In</button>
            </form>
        </div>
    </div>
</body>
</html>
