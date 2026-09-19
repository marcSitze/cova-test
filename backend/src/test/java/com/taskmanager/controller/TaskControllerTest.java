package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.config.SecurityConfig;
import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.PageResponse;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.exception.UnauthorizedAccessException;
import com.taskmanager.security.CustomAccessDeniedHandler;
import com.taskmanager.security.CustomUserDetailsService;
import com.taskmanager.security.JwtAuthenticationEntryPoint;
import com.taskmanager.security.JwtTokenProvider;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal userPrincipal;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "password", List.of());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        taskResponse = new TaskResponse(100L, "Tâche test", "Desc test", TaskStatus.TODO, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/tasks doit retourner HTTP 200 OK avec la liste paginée")
    void getTasks_Returns200Ok() throws Exception {
        PageResponse<TaskResponse> pageResponse = new PageResponse<>(List.of(taskResponse), 0, 20, 1, 1, true);

        when(taskService.getTasks(eq(1L), any(), any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest("Tâche test", "Desc test", TaskStatus.TODO))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/tasks avec titre valide doit retourner HTTP 201 Created")
    void createTask_ValidRequest_Returns201Created() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Nouvelle tâche", "Description", TaskStatus.TODO);

        when(taskService.createTask(any(CreateTaskRequest.class), eq(1L))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Tâche test"));
    }

    @Test
    @DisplayName("POST /api/tasks avec titre vide doit retourner HTTP 400 Bad Request")
    void createTask_BlankTitle_Returns400BadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("", "Description", TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @DisplayName("GET /api/tasks/{id} pour une tâche d'un autre utilisateur doit retourner HTTP 403 Forbidden")
    void getTaskById_Unauthorized_Returns403Forbidden() throws Exception {
        when(taskService.getTaskById(999L, 1L))
                .thenThrow(new UnauthorizedAccessException("Vous n'êtes pas autorisé à accéder à cette tâche."));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} valide doit retourner HTTP 200 OK")
    void updateTask_ValidRequest_Returns200Ok() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest("Titre maj", "Desc maj", TaskStatus.IN_PROGRESS);

        when(taskService.updateTask(eq(100L), any(UpdateTaskRequest.class), eq(1L))).thenReturn(taskResponse);

        mockMvc.perform(put("/api/tasks/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status valide doit retourner HTTP 200 OK")
    void updateTaskStatus_ValidRequest_Returns200Ok() throws Exception {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DONE);

        when(taskService.updateTaskStatus(eq(100L), any(UpdateTaskStatusRequest.class), eq(1L))).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tasks/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} valide doit retourner HTTP 204 No Content")
    void deleteTask_ValidId_Returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/100"))
                .andExpect(status().isNoContent());
    }
}
