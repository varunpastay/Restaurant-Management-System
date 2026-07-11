<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Restaurant Settings - ${fn:escapeXml(restaurant.name)}</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body>
<div class="d-flex">
    <%@ include file="/WEB-INF/jsp/common/_admin_nav.jspf" %>

    <div class="admin-content p-4" style="max-width:820px;">
        <h4 class="fw-bold mb-1">Restaurant Settings</h4>
        <p class="text-muted-brand mb-4">Everything here is editable at any time - no code changes or redeploys needed.</p>

        <c:if test="${not empty error}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/settings" enctype="multipart/form-data">
            <div class="card mb-3">
                <div class="card-header fw-bold">Branding</div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label">Restaurant Name *</label>
                        <input type="text" name="name" class="form-control" value="${fn:escapeXml(restaurant.name)}" required>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Logo</label>
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <img src="${pageContext.request.contextPath}${not empty restaurant.logoPath ? restaurant.logoPath : '/uploads/none.png'}"
                                     class="restaurant-logo" alt="logo">
                                <input type="file" name="logo" accept="image/png,image/jpeg,image/webp" class="form-control">
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Banner</label>
                            <input type="file" name="banner" accept="image/png,image/jpeg,image/webp" class="form-control">
                        </div>
                    </div>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-header fw-bold">Contact &amp; Legal</div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label">Address</label>
                        <input type="text" name="address" class="form-control" value="${fn:escapeXml(restaurant.address)}">
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Phone</label>
                            <input type="text" name="phone" class="form-control" value="${fn:escapeXml(restaurant.phone)}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" name="email" class="form-control" value="${fn:escapeXml(restaurant.email)}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">GST Number</label>
                            <input type="text" name="gstin" class="form-control" value="${fn:escapeXml(restaurant.gstin)}">
                        </div>
                    </div>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-header fw-bold">Currency &amp; Charges</div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Currency Code</label>
                            <input type="text" name="currencyCode" class="form-control" value="${fn:escapeXml(restaurant.currencyCode)}" maxlength="10">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Currency Symbol</label>
                            <input type="text" name="currencySymbol" class="form-control" value="${fn:escapeXml(restaurant.currencySymbol)}" maxlength="5">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Service Charge %</label>
                            <input type="number" step="0.01" min="0" name="serviceChargePercent" class="form-control" value="${restaurant.serviceChargePercent}">
                        </div>
                    </div>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-header fw-bold">Appearance &amp; Hours</div>
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Theme Color</label><br>
                            <input type="color" name="themeColor" class="color-swatch" value="${fn:escapeXml(restaurant.themeColor)}">
                        </div>
                        <div class="col-md-3 mb-3">
                            <div class="form-check form-switch mt-4">
                                <input class="form-check-input" type="checkbox" name="darkModeDefault" id="darkModeDefault" ${restaurant.darkModeDefault ? 'checked' : ''}>
                                <label class="form-check-label" for="darkModeDefault">Dark mode by default</label>
                            </div>
                        </div>
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Opening Time</label>
                            <input type="time" name="openingTime" class="form-control" value="${restaurant.openingTime}">
                        </div>
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Closing Time</label>
                            <input type="time" name="closingTime" class="form-control" value="${restaurant.closingTime}">
                        </div>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="open" id="isOpen" ${restaurant.open ? 'checked' : ''}>
                        <label class="form-check-label" for="isOpen">Restaurant is currently accepting orders</label>
                    </div>
                </div>
            </div>

            <button type="submit" class="btn btn-brand">Save Settings</button>
        </form>

        <div class="card mt-4">
            <div class="card-header fw-bold">Taxes</div>
            <div class="card-body">
                <div class="table-responsive">
                <table class="table table-sm align-middle">
                    <thead><tr><th>Name</th><th>Percent</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${taxes}" var="tax">
                        <tr>
                            <td>${fn:escapeXml(tax.name)}</td>
                            <td>${tax.percent}%</td>
                            <td>
                                <span class="badge ${tax.active ? 'text-bg-success' : 'text-bg-secondary'}">${tax.active ? 'Active' : 'Inactive'}</span>
                            </td>
                            <td class="text-end text-nowrap">
                                <form method="post" action="${pageContext.request.contextPath}/admin/settings/tax" class="d-inline">
                                    <input type="hidden" name="action" value="toggle">
                                    <input type="hidden" name="taxId" value="${tax.taxId}">
                                    <button type="submit" class="btn btn-sm btn-outline-brand">${tax.active ? 'Disable' : 'Enable'}</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/admin/settings/tax" class="d-inline"
                                      onsubmit="return confirm('Delete this tax?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="taxId" value="${tax.taxId}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/settings/tax" class="row g-2 mt-2">
                    <input type="hidden" name="action" value="add">
                    <div class="col-5">
                        <input type="text" name="name" class="form-control" placeholder="Tax name (e.g. CGST)" required>
                    </div>
                    <div class="col-4">
                        <input type="number" step="0.01" min="0" name="percent" class="form-control" placeholder="Percent" required>
                    </div>
                    <div class="col-3">
                        <button type="submit" class="btn btn-outline-brand w-100">Add Tax</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
