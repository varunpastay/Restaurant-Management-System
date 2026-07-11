<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Page Not Found</title>
    <style>
        body { font-family: Arial, Helvetica, sans-serif; background:#f7f4ef; color:#3a2e26; text-align:center; padding:80px 20px; }
        h1 { font-size:64px; margin:0; color:#c0392b; }
        p { font-size:18px; }
        a { color:#c0392b; text-decoration:none; font-weight:bold; }
    </style>
</head>
<body>
    <h1>404</h1>
    <p>The page you're looking for doesn't exist.</p>
    <p>If you scanned a table QR code, please ask a staff member for help.</p>
    <p><a href="${pageContext.request.contextPath}/">Go to homepage</a></p>
</body>
</html>
