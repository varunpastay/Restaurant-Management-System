<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Backup &amp; Restore - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:700px;">
        <h4 class="fw-bold mb-3">Backup &amp; Restore</h4>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <div class="card mb-4">
            <div class="card-header fw-bold">Backup</div>
            <div class="card-body">
                <p class="text-muted-brand">Download a complete snapshot of your restaurant's database (menu, tables, orders, settings, everything) as a .sql file. Keep it somewhere safe.</p>
                <a href="${pageContext.request.contextPath}/admin/backup/download" class="btn btn-brand">
                    <i class="bi bi-download"></i> Download Backup
                </a>
            </div>
        </div>

        <div class="card border-danger">
            <div class="card-header fw-bold text-danger">Restore (Destructive)</div>
            <div class="card-body">
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    <strong>Warning:</strong> Restoring replaces ALL current data - menu, orders, settings, everything -
                    with the contents of the uploaded file. This cannot be undone. Only restore a backup you trust.
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/backup/restore" enctype="multipart/form-data"
                      onsubmit="return confirm('This will permanently overwrite all current data. Are you absolutely sure?');">
                    <div class="mb-3">
                        <label class="form-label small">Backup File (.sql)</label>
                        <input type="file" name="backupFile" accept=".sql" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small">Type <strong>RESTORE</strong> to confirm</label>
                        <input type="text" name="confirmText" class="form-control" placeholder="RESTORE" required>
                    </div>
                    <button type="submit" class="btn btn-outline-danger">
                        <i class="bi bi-exclamation-triangle"></i> Restore Database
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
