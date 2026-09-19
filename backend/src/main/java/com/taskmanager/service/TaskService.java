package com.taskmanager.service;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.PageResponse;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    PageResponse<TaskResponse> getTasks(Long userId, TaskStatus status, String search, Pageable pageable);

    TaskResponse getTaskById(Long taskId, Long userId);

    TaskResponse createTask(CreateTaskRequest request, Long userId);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long userId);

    TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, Long userId);

    void deleteTask(Long taskId, Long userId);
}
