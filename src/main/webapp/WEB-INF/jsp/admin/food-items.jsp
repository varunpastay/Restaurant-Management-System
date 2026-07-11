<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Food Items - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="fw-bold mb-0">Food Items</h4>
            <a href="${pageContext.request.contextPath}/admin/food/form" class="btn btn-brand">
                <i class="bi bi-plus-lg"></i> Add Food Item
            </a>
        </div>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <c:if test="${empty items}">
            <div class="text-center py-5 text-muted-brand">
                <i class="bi bi-egg-fried empty-state-icon"></i>
                <p class="mt-2">No food items yet. Add your first dish above.</p>
            </div>
        </c:if>
        <c:if test="${not empty items}">
        <div class="table-responsive">
        <table class="table align-middle">
            <thead><tr><th></th><th>Name</th><th>Category</th><th>Price</th><th>Type</th><th>Badges</th><th>Status</th><th></th></tr></thead>
            <tbody>
            <c:forEach items="${items}" var="f">
                <tr>
                    <td>
                        <img src="${pageContext.request.contextPath}${not empty f.primaryImagePath ? f.primaryImagePath : '/uploads/none.jpg'}"
                             style="width:44px;height:44px;object-fit:cover;border-radius:6px;" alt="">
                    </td>
                    <td>${fn:escapeXml(f.name)}</td>
                    <td>${fn:escapeXml(f.categoryName)}</td>
                    <td class="text-nowrap">
                        <c:if test="${f.offerPrice != null}"><span class="price-strike">${f.price}</span> </c:if>
                        ${f.effectivePrice}
                    </td>
                    <td>${f.foodType}</td>
                    <td class="text-nowrap">
                        <c:if test="${f.recommended}"><span class="badge badge-recommended">Rec.</span></c:if>
                        <c:if test="${f.bestseller}"><span class="badge badge-bestseller">Best</span></c:if>
                    </td>
                    <td><span class="badge ${f.available ? 'text-bg-success' : 'text-bg-secondary'}">${f.available ? 'Available' : 'Unavailable'}</span></td>
                    <td class="text-end text-nowrap">
                        <a href="${pageContext.request.contextPath}/admin/food/form?id=${f.foodItemId}" class="btn btn-sm btn-outline-brand">Edit</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/food" class="d-inline">
                            <input type="hidden" name="action" value="toggleAvailability">
                            <input type="hidden" name="foodItemId" value="${f.foodItemId}">
                            <button type="submit" class="btn btn-sm btn-outline-secondary">${f.available ? 'Hide' : 'Show'}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/food" class="d-inline"
                              onsubmit="return confirm('Delete this food item?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="foodItemId" value="${f.foodItemId}">
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
