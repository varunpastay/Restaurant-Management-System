<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <title>Bill - ${fn:escapeXml(order.orderNo)}</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
    <style>
        @media print {
            .no-print { display: none !important; }
            body { background: #fff !important; }
        }
    </style>
</head>
<body>

<header class="restaurant-header py-2 no-print">
    <div class="container d-flex align-items-center justify-content-between">
        <a href="${pageContext.request.contextPath}/counter/dashboard" class="btn btn-outline-brand btn-sm">
            <i class="bi bi-arrow-left"></i> Dashboard
        </a>
        <a href="${pageContext.request.contextPath}/staff/logout" class="btn btn-outline-brand btn-sm">Logout</a>
    </div>
</header>

<div class="container py-4" style="max-width:640px;">
    <div class="card animate-in">
        <div class="card-body">
            <div class="text-center mb-3">
                <h4 class="mb-0 fw-bold">${fn:escapeXml(restaurant.name)}</h4>
                <c:if test="${not empty restaurant.address}"><small class="text-muted-brand">${fn:escapeXml(restaurant.address)}</small><br></c:if>
                <small class="text-muted-brand">
                    <c:if test="${not empty restaurant.phone}">${fn:escapeXml(restaurant.phone)} &middot; </c:if>
                    <c:if test="${not empty restaurant.gstin}">GSTIN: ${fn:escapeXml(restaurant.gstin)}</c:if>
                </small>
            </div>

            <div class="d-flex justify-content-between mb-1">
                <span class="text-muted-brand">Order No</span><strong>${fn:escapeXml(order.orderNo)}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
                <span class="text-muted-brand">Table</span><strong>${fn:escapeXml(order.tableNo)}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
                <span class="text-muted-brand">Date</span><span>${fn:substring(fn:replace(order.createdAt, 'T', ' '), 0, 16)}</span>
            </div>
            <c:if test="${not empty payment}">
                <div class="d-flex justify-content-between mb-1">
                    <span class="text-muted-brand">Invoice No</span><strong>${fn:escapeXml(payment.invoiceNo)}</strong>
                </div>
                <div class="d-flex justify-content-between mb-1">
                    <span class="text-muted-brand">Payment Method</span><span>${fn:escapeXml(payment.method)}</span>
                </div>
            </c:if>

            <div class="my-3">
                <span class="status-chip
                    ${order.status == 'CANCELLED' ? 'status-cancelled' : (not empty payment ? 'status-ready' : 'status-preparing')}">
                    ${order.status == 'CANCELLED' ? 'Cancelled' : (not empty payment ? 'Paid' : 'Unpaid')}
                </span>
            </div>

            <hr>
            <table class="table table-sm">
                <thead>
                <tr><th>Item</th><th class="text-center">Qty</th><th class="text-end">Amount</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${order.items}" var="item">
                    <tr>
                        <td>
                            ${fn:escapeXml(item.foodNameSnapshot)}
                            <c:if test="${not empty item.specialInstructions}">
                                <div class="small text-muted-brand">Note: ${fn:escapeXml(item.specialInstructions)}</div>
                            </c:if>
                        </td>
                        <td class="text-center">${item.quantity}</td>
                        <td class="text-end">${restaurant.currencySymbol}${item.lineTotal}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <c:if test="${not empty order.customerNote}">
                <div class="small text-muted-brand mb-2"><i class="bi bi-sticky"></i> Order note: ${fn:escapeXml(order.customerNote)}</div>
            </c:if>
            <hr>

            <div class="d-flex justify-content-between small"><span class="text-muted-brand">Subtotal</span><span>${restaurant.currencySymbol}${order.subtotal}</span></div>
            <c:if test="${order.taxAmount > 0}">
                <div class="d-flex justify-content-between small">
                    <span class="text-muted-brand">
                        Tax
                        <c:if test="${not empty taxes}">
                            (<c:forEach items="${taxes}" var="tax" varStatus="loopStatus">${fn:escapeXml(tax.name)} ${tax.percent}%<c:if test="${!loopStatus.last}"> + </c:if></c:forEach>)
                        </c:if>
                    </span>
                    <span>${restaurant.currencySymbol}${order.taxAmount}</span>
                </div>
            </c:if>
            <c:if test="${order.serviceChargeAmount > 0}">
                <div class="d-flex justify-content-between small"><span class="text-muted-brand">Service Charge</span><span>${restaurant.currencySymbol}${order.serviceChargeAmount}</span></div>
            </c:if>
            <c:if test="${order.discountAmount > 0}">
                <div class="d-flex justify-content-between small text-success"><span>Discount</span><span>-${restaurant.currencySymbol}${order.discountAmount}</span></div>
            </c:if>
            <div class="d-flex justify-content-between fw-bold fs-5 mt-2 pt-2 border-top"><span>Grand Total</span><span class="brand-text">${restaurant.currencySymbol}${order.grandTotal}</span></div>

            <div class="text-center text-muted-brand small mt-4">Thank you for dining with us!</div>
        </div>
    </div>

    <div class="no-print mt-3 d-flex flex-column gap-2">
        <c:if test="${order.status == 'SERVED' and empty payment}">
            <div class="card animate-in" style="animation-delay:0.05s">
                <div class="card-body">
                    <label class="form-label small fw-semibold">Payment Method</label>
                    <select id="paymentMethod" class="form-select mb-2">
                        <option value="CASH">Cash</option>
                        <option value="CARD">Card</option>
                        <option value="UPI">UPI</option>
                        <option value="OTHER">Other</option>
                    </select>
                    <button id="markPaidBtn" type="button" class="btn btn-brand w-100" data-order-id="${order.orderId}">
                        <i class="bi bi-check-circle"></i> Mark Paid
                    </button>
                </div>
            </div>
            <button id="cancelBtn" type="button" class="btn btn-outline-danger" data-order-id="${order.orderId}">
                <i class="bi bi-x-circle"></i> Cancel Order
            </button>
        </c:if>

        <button type="button" class="btn btn-outline-brand" onclick="window.print()">
            <i class="bi bi-printer"></i> Print
        </button>
        <a class="btn btn-outline-brand" href="${pageContext.request.contextPath}/counter/invoice.pdf?orderId=${order.orderId}" target="_blank">
            <i class="bi bi-file-earmark-pdf"></i> ${not empty payment ? 'Reprint' : 'Download'} PDF Invoice
        </a>
        <div id="billActionError" class="text-danger small d-none"></div>
    </div>
</div>

<script>
    window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/bill.js"></script>
</body>
</html>
