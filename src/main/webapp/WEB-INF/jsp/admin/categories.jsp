<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Categories - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:820px;">
        <h4 class="fw-bold mb-3">Categories</h4>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <div class="card mb-4">
            <div class="card-header fw-bold">${not empty editing ? 'Edit Category' : 'Add Category'}</div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/categories" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="categoryId" value="${editing.categoryId}">
                    <div class="row g-2">
                        <div class="col-md-5">
                            <label class="form-label small">Name *</label>
                            <input type="text" name="name" class="form-control" value="${fn:escapeXml(editing.name)}" required>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small">Display Order</label>
                            <input type="number" name="displayOrder" class="form-control" value="${empty editing ? 0 : editing.displayOrder}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small">Image</label>
                            <input type="file" name="image" accept="image/png,image/jpeg,image/webp" class="form-control">
                        </div>
                        <div class="col-md-2 d-flex align-items-end">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" name="active" id="catActive"
                                       ${empty editing or editing.active ? 'checked' : ''}>
                                <label class="form-check-label small" for="catActive">Active</label>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-brand mt-3">${not empty editing ? 'Save Changes' : 'Add Category'}</button>
                    <c:if test="${not empty editing}">
                        <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-outline-secondary mt-3">Cancel</a>
                    </c:if>
                </form>
            </div>
        </div>

        <c:if test="${empty categories}">
            <div class="text-center py-5 text-muted-brand">
                <i class="bi bi-tags empty-state-icon"></i>
                <p class="mt-2">No categories yet. Add your first one above.</p>
            </div>
        </c:if>
        <c:if test="${not empty categories}">
        <div class="table-responsive">
        <table class="table align-middle">
            <thead><tr><th></th><th>Name</th><th>Order</th><th>Status</th><th></th></tr></thead>
            <tbody>
            <c:forEach items="${categories}" var="c">
                <tr>
                    <td>
                        <img src="${pageContext.request.contextPath}${not empty c.imagePath ? c.imagePath : '/uploads/none.jpg'}"
                             style="width:44px;height:44px;object-fit:cover;border-radius:6px;" alt="">
                    </td>
                    <td>${fn:escapeXml(c.name)}</td>
                    <td>${c.displayOrder}</td>
                    <td><span class="badge ${c.active ? 'text-bg-success' : 'text-bg-secondary'}">${c.active ? 'Active' : 'Inactive'}</span></td>
                    <td class="text-end text-nowrap">
                        <a href="${pageContext.request.contextPath}/admin/categories?edit=${c.categoryId}" class="btn btn-sm btn-outline-brand">Edit</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/categories" class="d-inline">
                            <input type="hidden" name="action" value="toggle">
                            <input type="hidden" name="categoryId" value="${c.categoryId}">
                            <button type="submit" class="btn btn-sm btn-outline-secondary">${c.active ? 'Disable' : 'Enable'}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/categories" class="d-inline"
                              onsubmit="return confirm('Delete this category? Food items in it will need to be moved or deleted first.');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="categoryId" value="${c.categoryId}">
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
