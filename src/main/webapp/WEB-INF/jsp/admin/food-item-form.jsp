<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>${not empty item ? 'Edit' : 'Add'} Food Item - Admin</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:760px;">
        <h4 class="fw-bold mb-3">${not empty item ? 'Edit' : 'Add'} Food Item</h4>
        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/food/form" enctype="multipart/form-data">
            <input type="hidden" name="foodItemId" value="${item.foodItemId}">

            <div class="card mb-3">
                <div class="card-body">
                    <div class="row g-2">
                        <div class="col-md-8 mb-2">
                            <label class="form-label small">Name *</label>
                            <input type="text" name="name" class="form-control" value="${fn:escapeXml(item.name)}" required>
                        </div>
                        <div class="col-md-4 mb-2">
                            <label class="form-label small">Category *</label>
                            <select name="categoryId" class="form-select" required>
                                <c:forEach items="${categories}" var="c">
                                    <option value="${c.categoryId}" ${item.categoryId == c.categoryId ? 'selected' : ''}>${fn:escapeXml(c.name)}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Description</label>
                        <textarea name="description" class="form-control" rows="2">${fn:escapeXml(item.description)}</textarea>
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Ingredients</label>
                        <textarea name="ingredients" class="form-control" rows="2">${fn:escapeXml(item.ingredients)}</textarea>
                    </div>
                    <div class="row g-2">
                        <div class="col-md-3 mb-2">
                            <label class="form-label small">Price *</label>
                            <input type="number" step="0.01" min="0" name="price" class="form-control" value="${item.price}" required>
                        </div>
                        <div class="col-md-3 mb-2">
                            <label class="form-label small">Offer Price</label>
                            <input type="number" step="0.01" min="0" name="offerPrice" class="form-control" value="${item.offerPrice}">
                        </div>
                        <div class="col-md-3 mb-2">
                            <label class="form-label small">Prep Time (min)</label>
                            <input type="number" min="0" name="prepTimeMinutes" class="form-control" value="${empty item ? 15 : item.prepTimeMinutes}">
                        </div>
                        <div class="col-md-3 mb-2">
                            <label class="form-label small">Display Order</label>
                            <input type="number" name="displayOrder" class="form-control" value="${empty item ? 0 : item.displayOrder}">
                        </div>
                    </div>
                    <div class="row g-2">
                        <div class="col-md-6 mb-2">
                            <label class="form-label small">Food Type</label>
                            <select name="foodType" class="form-select">
                                <c:forEach items="${foodTypes}" var="ft">
                                    <option value="${ft}" ${item.foodType == ft ? 'selected' : ''}>${ft}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6 mb-2">
                            <label class="form-label small">Spice Level</label>
                            <select name="spiceLevel" class="form-select">
                                <c:forEach items="${spiceLevels}" var="sl">
                                    <option value="${sl}" ${item.spiceLevel == sl ? 'selected' : ''}>${sl}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="d-flex gap-4 mt-2">
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="available" id="available" ${empty item or item.available ? 'checked' : ''}>
                            <label class="form-check-label" for="available">Available</label>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="recommended" id="recommended" ${item.recommended ? 'checked' : ''}>
                            <label class="form-check-label" for="recommended">Recommended</label>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="bestseller" id="bestseller" ${item.bestseller ? 'checked' : ''}>
                            <label class="form-check-label" for="bestseller">Bestseller</label>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-header fw-bold">Photos</div>
                <div class="card-body">
                    <c:if test="${not empty images}">
                        <div class="d-flex flex-wrap gap-3 mb-3">
                            <c:forEach items="${images}" var="img">
                                <div class="text-center">
                                    <img src="${pageContext.request.contextPath}${img.imagePath}"
                                         style="width:90px;height:90px;object-fit:cover;border-radius:8px;" alt="">
                                    <div class="small mt-1">
                                        <c:if test="${img.primary}"><span class="badge text-bg-success">Primary</span></c:if>
                                    </div>
                                    <div class="d-flex gap-1 mt-1">
                                        <c:if test="${!img.primary}">
                                            <form method="post" action="${pageContext.request.contextPath}/admin/food/images">
                                                <input type="hidden" name="action" value="setPrimary">
                                                <input type="hidden" name="foodItemId" value="${item.foodItemId}">
                                                <input type="hidden" name="foodImageId" value="${img.foodImageId}">
                                                <button type="submit" class="btn btn-sm btn-outline-brand">Make Primary</button>
                                            </form>
                                        </c:if>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/food/images"
                                              onsubmit="return confirm('Remove this photo?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="foodItemId" value="${item.foodItemId}">
                                            <input type="hidden" name="foodImageId" value="${img.foodImageId}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">Remove</button>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>
                    <label class="form-label small">Add Photo(s)</label>
                    <input type="file" name="images" accept="image/png,image/jpeg,image/webp" class="form-control" multiple>
                    <c:if test="${empty item}">
                        <small class="text-muted-brand">You can add more photos after saving too.</small>
                    </c:if>
                </div>
            </div>

            <button type="submit" class="btn btn-brand">${not empty item ? 'Save Changes' : 'Add Food Item'}</button>
            <a href="${pageContext.request.contextPath}/admin/food" class="btn btn-outline-secondary">Cancel</a>
        </form>
    </div>
</div>
</body>
</html>
