package com.portfolio.automation_sut.service;

import com.portfolio.automation_sut.dto.LabelDtos.LabelResponse;
import com.portfolio.automation_sut.model.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {
    public LabelResponse toResponse(Label label) {
        return new LabelResponse(label.getId(), label.getName(), label.getColor());
    }
}
