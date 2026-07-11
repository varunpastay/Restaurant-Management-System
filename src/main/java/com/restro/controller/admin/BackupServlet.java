package com.restro.controller.admin;

import com.restro.utility.FlashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Landing page for Admin > Backup & Restore - actual work happens in BackupDownloadServlet / RestoreServlet. */
@WebServlet(name = "BackupServlet", urlPatterns = {"/admin/backup"})
public class BackupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        request.setAttribute("error", FlashUtil.consumeError(session));
        request.setAttribute("success", FlashUtil.consumeSuccess(session));
        request.setAttribute("activeNav", "backup");
        request.getRequestDispatcher("/WEB-INF/jsp/admin/backup.jsp").forward(request, response);
    }
}
