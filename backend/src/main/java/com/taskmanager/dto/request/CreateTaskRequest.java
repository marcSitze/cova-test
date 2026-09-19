package com.taskmanager.dto.request;

import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Le titre de la tâche est obligatoire")
        @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
        String title,

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        String description,

        TaskStatus status
) {}
