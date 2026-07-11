<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Table ${fn:escapeXml(table.tableNo)} QR Code</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
    <style>
        @media print {
            .no-print { display: none !important; }
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="text-center">
        <h4 class="mb-1">${fn:escapeXml(restaurant.name)}</h4>
        <p class="text-muted-brand mb-3">Scan to view menu &amp; order</p>
        <img src="${pageContext.request.contextPath}${qr.imagePath}" style="width:280px;height:280px;" alt="QR for table ${fn:escapeXml(table.tableNo)}">
        <h3 class="mt-3">Table ${fn:escapeXml(table.tableNo)}</h3>

        <div class="no-print mt-4">
            <button type="button" class="btn btn-brand" onclick="window.print()"><i class="bi bi-printer"></i> Print</button>
        </div>
    </div>
</body>
</html>
