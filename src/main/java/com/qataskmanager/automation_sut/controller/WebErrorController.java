package com.qataskmanager.automation_sut.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebErrorController implements ErrorController {
    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttribute == null ? 500 : Integer.parseInt(statusAttribute.toString());
        HttpStatus httpStatus = HttpStatus.resolve(status);
        String title = status == 403 ? "Access denied" : status == 404 ? "Page not found" : "Unexpected error";
        String message = status == 403
                ? "You do not have permission to access this page."
                : status == 404
                ? "The page you requested does not exist."
                : "The SUT could not complete the request.";
        model.addAttribute("status", status);
        model.addAttribute("error", httpStatus == null ? "Error" : httpStatus.getReasonPhrase());
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        return "error/system";
    }
}
