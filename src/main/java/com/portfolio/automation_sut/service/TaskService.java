package com.portfolio.automation_sut.service;

import com.portfolio.automation_sut.dto.TaskDtos.TaskRequest;
import com.portfolio.automation_sut.dto.TaskDtos.TaskResponse;
import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.model.Role;
import com.portfolio.automation_sut.model.Task;
import com.portfolio.automation_sut.model.TaskStatus;
import com.portfolio.automation_sut.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, CurrentUserService currentUserService, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.taskMapper = taskMapper;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listVisibleTasks() {
        AppUser currentUser = currentUserService.getCurrentUser();
        List<Task> tasks = isAdmin(currentUser)
                ? taskRepository.findAllByOrderById()
                : taskRepository.findByOwnerOrderById(currentUser);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getVisibleTask(Long id) {
        return taskMapper.toResponse(findAuthorizedTask(id));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        AppUser owner = currentUserService.getCurrentUser();
        Task task = new Task(request.title(), request.status(), request.priority(), request.dueDate(), owner);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateVisibleTask(Long id, TaskRequest request) {
        Task task = findAuthorizedTask(id);
        task.update(request.title(), request.status(), request.priority(), request.dueDate());
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse completeVisibleTask(Long id) {
        Task task = findAuthorizedTask(id);
        task.update(task.getTitle(), TaskStatus.DONE, task.getPriority(), task.getDueDate());
        return taskMapper.toResponse(task);
    }

    @Transactional
    public void deleteVisibleTask(Long id) {
        Task task = findAuthorizedTask(id);
        taskRepository.delete(task);
    }

    private Task findAuthorizedTask(Long id) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        if (!isAdmin(currentUser) && !task.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot access tasks belonging to another user");
        }
        return task;
    }

    private boolean isAdmin(AppUser user) {
        return user.getRole() == Role.ADMIN;
    }
}
