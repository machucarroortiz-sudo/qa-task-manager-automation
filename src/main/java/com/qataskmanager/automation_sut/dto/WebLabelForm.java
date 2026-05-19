package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.dto.LabelDtos.LabelRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WebLabelForm {
    @NotBlank
    @Size(max = 40)
    private String name;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be a hex value like #2868c7")
    private String color;

    public WebLabelForm() {
    }

    public WebLabelForm(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public LabelRequest toLabelRequest() {
        return new LabelRequest(name, color);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
