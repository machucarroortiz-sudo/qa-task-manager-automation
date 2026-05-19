package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerOrderById(AppUser owner);
    List<Task> findAllByOrderById();
}
