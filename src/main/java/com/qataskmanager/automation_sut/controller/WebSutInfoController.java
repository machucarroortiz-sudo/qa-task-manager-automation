package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.service.ShellModelService;
import com.qataskmanager.automation_sut.service.SutInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebSutInfoController {
    private final SutInfoService sutInfoService;
    private final ShellModelService shellModelService;

    public WebSutInfoController(SutInfoService sutInfoService, ShellModelService shellModelService) {
        this.sutInfoService = sutInfoService;
        this.shellModelService = shellModelService;
    }

    @GetMapping("/sut-info")
    public String sutInfo(Model model) {
        shellModelService.addShellAttributes(model);
        model.addAttribute("sutInfo", sutInfoService.getSutInfo());
        return "sut-info";
    }
}
