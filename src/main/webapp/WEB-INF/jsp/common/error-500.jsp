<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Something Went Wrong</title>
    <style>
        body { font-family: Arial, Helvetica, sans-serif; background:#f7f4ef; color:#3a2e26; text-align:center; padding:80px 20px; }
        h1 { font-size:56px; margin:0; color:#c0392b; }
        p { font-size:18px; }
        a { color:#c0392b; text-decoration:none; font-weight:bold; }
    </style>
</head>
<body>
    <h1>Oops!</h1>
    <p>Something went wrong on our end. The kitchen has been notified.</p>
    <p>Please try again in a moment, or ask a staff member for assistance.</p>
    <p><a href="${pageContext.request.contextPath}/">Go to homepage</a></p>
</body>
</html>
