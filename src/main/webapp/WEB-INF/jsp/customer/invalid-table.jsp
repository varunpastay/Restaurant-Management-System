<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Table Not Found</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="text-center p-4 animate-in" style="max-width:420px;">
        <i class="bi bi-qr-code-scan brand-text" style="font-size:3.4rem;"></i>
        <h3 class="mt-3 fw-bold">We couldn't load that page</h3>
        <p class="text-muted-brand">${empty reason ? 'Please rescan the table QR code.' : fn:escapeXml(reason)}</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-brand mt-2">
            <i class="bi bi-house-door"></i> Go to homepage
        </a>
    </div>
</body>
</html>
