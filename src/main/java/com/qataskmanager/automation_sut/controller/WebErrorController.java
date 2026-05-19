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
        String titleKey = status == 403 ? "error.accessDenied.title" : status == 404 ? "error.notFound.title" : "error.unexpected.title";
        String messageKey = status == 403 ? "error.accessDenied.message" : status == 404 ? "error.notFound.message" : "error.unexpected.message";
        model.addAttribute("status", status);
        model.addAttribute("error", httpStatus == null ? "Error" : httpStatus.getReasonPhrase());
        model.addAttribute("titleKey", titleKey);
        model.addAttribute("messageKey", messageKey);
        return "error/system";
    }
}
