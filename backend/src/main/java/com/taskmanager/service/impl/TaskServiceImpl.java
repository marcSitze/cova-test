package com.taskmanager.service.impl;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.PageResponse;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.TaskEntity;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.UserEntity;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedAccessException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.specification.TaskSpecification;
import com.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(Long userId, TaskStatus status, String search, Pageable pageable) {
        log.debug("Récupération des tâches pour l'utilisateur ID: {}, status: {}, search: {}", userId, status, search);
        Specification<TaskEntity> spec = TaskSpecification.build(userId, status, search);
        Page<TaskEntity> taskPage = taskRepository.findAll(spec, pageable);
        return PageResponse.from(taskPage.map(taskMapper::toTaskResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, Long userId) {
        TaskEntity task = findTaskAndVerifyOwnership(taskId, userId);
        return taskMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        TaskEntity task = taskMapper.toTaskEntity(request, user);
        TaskEntity savedTask = taskRepository.save(task);
        log.info("Nouvelle tâche créée avec l'ID: {} pour l'utilisateur ID: {}", savedTask.getId(), userId);

        return taskMapper.toTaskResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long userId) {
        TaskEntity task = findTaskAndVerifyOwnership(taskId, userId);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());

        TaskEntity updatedTask = taskRepository.save(task);
        log.info("Tâche ID: {} mise à jour par l'utilisateur ID: {}", taskId, userId);

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, Long userId) {
        TaskEntity task = findTaskAndVerifyOwnership(taskId, userId);

        task.setStatus(request.status());

        TaskEntity updatedTask = taskRepository.save(task);
        log.info("Statut de la tâche ID: {} modifié vers {} par l'utilisateur ID: {}", taskId, request.status(), userId);

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        TaskEntity task = findTaskAndVerifyOwnership(taskId, userId);
        taskRepository.delete(task);
        log.info("Tâche ID: {} supprimée par l'utilisateur ID: {}", taskId, userId);
    }

    private TaskEntity findTaskAndVerifyOwnership(Long taskId, Long userId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (!task.getUser().getId().equals(userId)) {
            log.warn("Tentative d'accès non autorisé par l'utilisateur ID: {} à la tâche ID: {} appartenant à l'utilisateur ID: {}",
                    userId, taskId, task.getUser().getId());
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à accéder ou modifier cette tâche.");
        }

        return task;
    }
}
