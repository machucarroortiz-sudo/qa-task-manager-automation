package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.Label;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findAllByOrderByName();
    Optional<Label> findByNameIgnoreCase(String name);
}
