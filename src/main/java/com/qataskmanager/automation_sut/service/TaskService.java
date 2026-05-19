package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.TaskDtos.TaskRequest;
import com.qataskmanager.automation_sut.dto.TaskDtos.TaskResponse;
import com.qataskmanager.automation_sut.dto.PaginationDtos.PagedResponse;
import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Role;
import com.qataskmanager.automation_sut.model.Task;
import com.qataskmanager.automation_sut.model.TaskPriority;
import com.qataskmanager.automation_sut.model.TaskStatus;
import com.qataskmanager.automation_sut.model.NotificationType;
import com.qataskmanager.automation_sut.repository.TaskRepository;
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
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, CurrentUserService currentUserService, TaskMapper taskMapper,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.taskMapper = taskMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listVisibleTasks() {
        return listVisibleTaskEntities().stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchVisibleTasks(TaskStatus status, TaskPriority priority, String search) {
        String normalizedSearch = normalize(search);
        return filterVisibleTaskEntities(status, priority, normalizedSearch).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> searchVisibleTasks(TaskStatus status, TaskPriority priority, String search, int page, int size) {
        String normalizedSearch = normalize(search);
        List<TaskResponse> filteredTasks = filterVisibleTaskEntities(status, priority, normalizedSearch).stream()
                .map(taskMapper::toResponse)
                .toList();
        return page(filteredTasks, page, size);
    }

    private List<Task> filterVisibleTaskEntities(TaskStatus status, TaskPriority priority, String normalizedSearch) {
        return listVisibleTaskEntities().stream()
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> priority == null || task.getPriority() == priority)
                .filter(task -> normalizedSearch.isBlank() || normalize(task.getTitle()).contains(normalizedSearch))
                .toList();
    }

    private List<Task> listVisibleTaskEntities() {
        AppUser currentUser = currentUserService.getCurrentUser();
        return isAdmin(currentUser)
                ? taskRepository.findAllByOrderById()
                : taskRepository.findByOwnerOrderById(currentUser);
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
        TaskStatus previousStatus = task.getStatus();
        task.update(request.title(), request.status(), request.priority(), request.dueDate());
        AppUser actor = currentUserService.getCurrentUser();
        String message = previousStatus == request.status()
                ? "Task updated: " + task.getTitle()
                : "Task status changed to " + request.status() + ": " + task.getTitle();
        notificationService.notifyIfDifferentUser(task.getOwner(), actor, NotificationType.TASK, message);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse completeVisibleTask(Long id) {
        Task task = findAuthorizedTask(id);
        task.update(task.getTitle(), TaskStatus.DONE, task.getPriority(), task.getDueDate());
        notificationService.notifyIfDifferentUser(
                task.getOwner(),
                currentUserService.getCurrentUser(),
                NotificationType.TASK,
                "Task status changed to DONE: " + task.getTitle()
        );
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private PagedResponse<TaskResponse> page(List<TaskResponse> items, int requestedPage, int requestedSize) {
        int safeSize = Math.max(1, Math.min(requestedSize, 100));
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil(items.size() / (double) safeSize);
        int safePage = totalPages == 0 ? 0 : Math.max(0, Math.min(requestedPage, totalPages - 1));
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        return new PagedResponse<>(
                items.subList(fromIndex, toIndex),
                safePage,
                safeSize,
                items.size(),
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1
        );
    }
}
