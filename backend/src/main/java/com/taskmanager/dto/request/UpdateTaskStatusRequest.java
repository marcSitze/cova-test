package com.taskmanager.dto.request;

import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Le statut de la tâche est obligatoire")
        TaskStatus status
) {}
