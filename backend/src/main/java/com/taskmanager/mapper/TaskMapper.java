package com.taskmanager.mapper;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.TaskEntity;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toTaskResponse(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TaskResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public TaskEntity toTaskEntity(CreateTaskRequest request, UserEntity user) {
        if (request == null) {
            return null;
        }
        return TaskEntity.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.TODO)
                .user(user)
                .build();
    }
}
