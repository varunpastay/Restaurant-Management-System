<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Staff - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:820px;">
        <h4 class="fw-bold mb-3">Staff Accounts</h4>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <div class="card mb-4">
            <div class="card-header fw-bold">${not empty editing ? 'Edit Staff Member' : 'Add Staff Member'}</div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/staff">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="staffId" value="${editing.staffId}">
                    <div class="row g-2">
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">Full Name *</label>
                            <input type="text" name="fullName" class="form-control" value="${fn:escapeXml(editing.fullName)}" required>
                        </div>
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">Email *</label>
                            <input type="email" name="email" class="form-control" value="${fn:escapeXml(editing.email)}" required>
                        </div>
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">${not empty editing ? 'New Password (leave blank to keep current)' : 'Password *'}</label>
                            <input type="password" name="password" class="form-control" ${empty editing ? 'required' : ''}>
                        </div>
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">Phone</label>
                            <input type="text" name="phone" class="form-control" value="${fn:escapeXml(editing.phone)}">
                        </div>
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">Role *</label>
                            <select name="role" class="form-select" required>
                                <c:forEach items="${roles}" var="r">
                                    <option value="${r}" ${editing.role == r ? 'selected' : ''}>${r}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end mb-2">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" name="active" id="staffActive" ${empty editing or editing.active ? 'checked' : ''}>
                                <label class="form-check-label small" for="staffActive">Active</label>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-brand mt-2">${not empty editing ? 'Save Changes' : 'Add Staff Member'}</button>
                    <c:if test="${not empty editing}">
                        <a href="${pageContext.request.contextPath}/admin/staff" class="btn btn-outline-secondary mt-2">Cancel</a>
                    </c:if>
                </form>
            </div>
        </div>

        <c:if test="${empty staffList}">
            <div class="text-center py-5 text-muted-brand">
                <i class="bi bi-people empty-state-icon"></i>
                <p class="mt-2">No staff accounts yet. Add your first one above.</p>
            </div>
        </c:if>
        <c:if test="${not empty staffList}">
        <div class="table-responsive">
        <table class="table align-middle">
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Phone</th><th>Status</th><th></th></tr></thead>
            <tbody>
            <c:forEach items="${staffList}" var="s">
                <tr>
                    <td>${fn:escapeXml(s.fullName)}</td>
                    <td>${fn:escapeXml(s.email)}</td>
                    <td><span class="badge text-bg-secondary">${s.role}</span></td>
                    <td>${fn:escapeXml(s.phone)}</td>
                    <td><span class="badge ${s.active ? 'text-bg-success' : 'text-bg-secondary'}">${s.active ? 'Active' : 'Inactive'}</span></td>
                    <td class="text-end text-nowrap">
                        <a href="${pageContext.request.contextPath}/admin/staff?edit=${s.staffId}" class="btn btn-sm btn-outline-brand">Edit</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/staff" class="d-inline"
                              onsubmit="return confirm('Delete this staff account?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="staffId" value="${s.staffId}">
                            <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
        </c:if>
    </div>
</div>
</body>
</html>
