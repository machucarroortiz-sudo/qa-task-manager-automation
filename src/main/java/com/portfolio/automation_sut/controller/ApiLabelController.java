package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.LabelDtos.LabelRequest;
import com.portfolio.automation_sut.dto.LabelDtos.LabelResponse;
import com.portfolio.automation_sut.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/labels")
public class ApiLabelController {
    private final LabelService labelService;

    public ApiLabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @Operation(summary = "List labels available for issue assignment")
    @GetMapping
    public List<LabelResponse> listLabels() {
        return labelService.listLabels();
    }

    @Operation(summary = "Create a label. Admin only.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(@Valid @RequestBody LabelRequest request) {
        LabelResponse response = labelService.createLabel(request);
        return ResponseEntity.created(URI.create("/api/labels/" + response.id())).body(response);
    }

    @Operation(summary = "Update a label. Admin only.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public LabelResponse updateLabel(@PathVariable Long id, @Valid @RequestBody LabelRequest request) {
        return labelService.updateLabel(id, request);
    }

    @Operation(summary = "Delete a label. Admin only.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}
