<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Tables &amp; QR Codes - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:900px;">
        <h4 class="fw-bold mb-3">Tables &amp; QR Codes</h4>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <div class="card mb-4">
            <div class="card-header fw-bold">${not empty editing ? 'Edit Table' : 'Add Table'}</div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/tables">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="tableId" value="${editing.tableId}">
                    <div class="row g-2">
                        <div class="col-md-4">
                            <label class="form-label small">Table No. / Name *</label>
                            <input type="text" name="tableNo" class="form-control" value="${fn:escapeXml(editing.tableNo)}" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small">Capacity</label>
                            <input type="number" min="1" name="capacity" class="form-control" value="${empty editing ? 4 : editing.capacity}">
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" name="active" id="tblActive" ${empty editing or editing.active ? 'checked' : ''}>
                                <label class="form-check-label small" for="tblActive">Active</label>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-brand mt-3">${not empty editing ? 'Save Changes' : 'Add Table'}</button>
                    <c:if test="${not empty editing}">
                        <a href="${pageContext.request.contextPath}/admin/tables" class="btn btn-outline-secondary mt-3">Cancel</a>
                    </c:if>
                </form>
            </div>
        </div>

        <div class="row g-3">
            <c:forEach items="${tables}" var="t">
                <div class="col-sm-6 col-lg-4">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h6 class="fw-bold mb-1">Table ${fn:escapeXml(t.tableNo)}</h6>
                            <div class="small text-muted-brand mb-2">
                                Seats ${t.capacity} &middot;
                                <span class="badge ${t.active ? 'text-bg-success' : 'text-bg-secondary'}">${t.active ? 'Active' : 'Inactive'}</span>
                            </div>

                            <c:choose>
                                <c:when test="${not empty qrByTable[t.tableId]}">
                                    <img src="${pageContext.request.contextPath}${qrByTable[t.tableId].imagePath}"
                                         style="width:140px;height:140px;object-fit:contain;" alt="QR for table ${fn:escapeXml(t.tableNo)}">
                                    <div class="d-flex flex-wrap justify-content-center gap-1 mt-2">
                                        <a href="${pageContext.request.contextPath}/admin/tables/qr/print?tableId=${t.tableId}"
                                           target="_blank" class="btn btn-sm btn-outline-brand">Print</a>
                                        <a href="${pageContext.request.contextPath}/admin/tables/qr/download?tableId=${t.tableId}"
                                           class="btn btn-sm btn-outline-brand">Download</a>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/tables/qr/generate" class="d-inline">
                                            <input type="hidden" name="tableId" value="${t.tableId}">
                                            <button type="submit" class="btn btn-sm btn-outline-secondary">Regenerate</button>
                                        </form>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-muted-brand py-4"><i class="bi bi-qr-code" style="font-size:2rem;"></i></div>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/tables/qr/generate">
                                        <input type="hidden" name="tableId" value="${t.tableId}">
                                        <button type="submit" class="btn btn-sm btn-brand">Generate QR</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>

                            <div class="d-flex justify-content-center gap-1 mt-3">
                                <a href="${pageContext.request.contextPath}/admin/tables?edit=${t.tableId}" class="btn btn-sm btn-outline-brand">Edit</a>
                                <form method="post" action="${pageContext.request.contextPath}/admin/tables"
                                      onsubmit="return confirm('Delete this table?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="tableId" value="${t.tableId}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>
</body>
</html>
