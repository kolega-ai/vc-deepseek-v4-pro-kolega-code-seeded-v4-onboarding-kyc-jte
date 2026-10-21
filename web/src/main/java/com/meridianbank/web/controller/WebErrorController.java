package com.meridianbank.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** Friendly error pages for 403/404/500. */
@Controller
public class WebErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handle(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = status == null ? 500 : Integer.parseInt(String.valueOf(status));
        model.addAttribute("message", switch (code) {
            case 403 -> "You do not have permission to view this page. If you believe this is a mistake, contact a bank administrator.";
            case 404 -> "That page does not exist or has moved.";
            default -> "Something went wrong on our side. Please try again shortly.";
        });
        return code == 403 ? "error/403" : code == 404 ? "error/404" : "error/500";
    }
}
