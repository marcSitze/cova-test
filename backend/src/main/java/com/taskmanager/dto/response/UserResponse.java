package com.taskmanager.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
