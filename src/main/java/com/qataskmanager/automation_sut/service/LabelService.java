package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.LabelDtos.LabelRequest;
import com.qataskmanager.automation_sut.dto.LabelDtos.LabelResponse;
import com.qataskmanager.automation_sut.model.Label;
import com.qataskmanager.automation_sut.repository.LabelRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public LabelService(LabelRepository labelRepository, LabelMapper labelMapper) {
        this.labelRepository = labelRepository;
        this.labelMapper = labelMapper;
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> listLabels() {
        return labelRepository.findAllByOrderByName().stream().map(labelMapper::toResponse).toList();
    }

    @Transactional
    public LabelResponse createLabel(LabelRequest request) {
        ensureNameIsAvailable(request.name(), null);
        return labelMapper.toResponse(labelRepository.save(new Label(request.name(), request.color())));
    }

    @Transactional
    public LabelResponse updateLabel(Long id, LabelRequest request) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label not found"));
        ensureNameIsAvailable(request.name(), id);
        label.update(request.name(), request.color());
        return labelMapper.toResponse(label);
    }

    @Transactional
    public void deleteLabel(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label not found"));
        labelRepository.delete(label);
    }

    private void ensureNameIsAvailable(String name, Long currentId) {
        labelRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new IllegalArgumentException("Label name already exists");
            }
        });
    }
}
