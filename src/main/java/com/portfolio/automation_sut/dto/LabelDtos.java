package com.portfolio.automation_sut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LabelDtos {
    public record LabelRequest(
            @Schema(example = "regression")
            @NotBlank
            @Size(max = 40)
            String name,

            @Schema(example = "#2868c7")
            @NotBlank
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be a hex value like #2868c7")
            String color
    ) {
    }

    public record LabelResponse(Long id, String name, String color) {
    }
}
