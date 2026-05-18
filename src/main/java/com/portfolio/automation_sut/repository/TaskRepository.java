package com.portfolio.automation_sut.repository;

import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.model.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderById(AppUser owner);
    List<Task> findAllByOrderById();
}
