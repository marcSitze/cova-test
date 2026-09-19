package com.taskmanager.service;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.PageResponse;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.TaskEntity;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.UserEntity;
import com.taskmanager.exception.UnauthorizedAccessException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UserEntity ownerUser;
    private UserEntity otherUser;
    private TaskEntity taskEntity;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        ownerUser = UserEntity.builder()
                .id(1L)
                .email("owner@example.com")
                .password("encoded_pass")
                .build();

        otherUser = UserEntity.builder()
                .id(2L)
                .email("other@example.com")
                .password("encoded_pass")
                .build();

        taskEntity = TaskEntity.builder()
                .id(100L)
                .title("Tâche de test")
                .description("Description de test")
                .status(TaskStatus.TODO)
                .user(ownerUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskResponse = new TaskResponse(100L, "Tâche de test", "Description de test", TaskStatus.TODO, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Créer une tâche valide pour l'utilisateur connecté")
    void createTask_Success() {
        CreateTaskRequest request = new CreateTaskRequest("Tâche de test", "Description de test", TaskStatus.TODO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(taskMapper.toTaskEntity(request, ownerUser)).thenReturn(taskEntity);
        when(taskRepository.save(taskEntity)).thenReturn(taskEntity);
        when(taskMapper.toTaskResponse(taskEntity)).thenReturn(taskResponse);

        TaskResponse response = taskService.createTask(request, 1L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Tâche de test", response.title());
        verify(taskRepository, times(1)).save(taskEntity);
    }

    @Test
    @DisplayName("Récupérer la liste des tâches paginées de l'utilisateur connecté")
    void getTasks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaskEntity> taskPage = new PageImpl<>(List.of(taskEntity), pageable, 1);

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toTaskResponse(taskEntity)).thenReturn(taskResponse);

        PageResponse<TaskResponse> response = taskService.getTasks(1L, TaskStatus.TODO, "test", pageable);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals("Tâche de test", response.content().get(0).title());
    }

    @Test
    @DisplayName("Obtenir une tâche appartenant à l'utilisateur connecté")
    void getTaskById_OwnedByUser_Success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));
        when(taskMapper.toTaskResponse(taskEntity)).thenReturn(taskResponse);

        TaskResponse response = taskService.getTaskById(100L, 1L);

        assertNotNull(response);
        assertEquals(100L, response.id());
    }

    @Test
    @DisplayName("Obtenir une tâche appartenant à un AUTRE utilisateur doit lever UnauthorizedAccessException (403)")
    void getTaskById_BelongsToAnotherUser_ThrowsUnauthorizedAccessException() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));

        assertThrows(UnauthorizedAccessException.class, () -> taskService.getTaskById(100L, 2L));
    }

    @Test
    @DisplayName("Mettre à jour une tâche appartenant à l'utilisateur connecté")
    void updateTask_OwnedByUser_Success() {
        UpdateTaskRequest request = new UpdateTaskRequest("Titre modifié", "Desc modifiée", TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(taskEntity)).thenReturn(taskEntity);
        when(taskMapper.toTaskResponse(taskEntity)).thenReturn(new TaskResponse(100L, "Titre modifié", "Desc modifiée", TaskStatus.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now()));

        TaskResponse response = taskService.updateTask(100L, request, 1L);

        assertNotNull(response);
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
    }

    @Test
    @DisplayName("Mettre à jour une tâche d'un AUTRE utilisateur doit lever UnauthorizedAccessException (403)")
    void updateTask_BelongsToAnotherUser_ThrowsUnauthorizedAccessException() {
        UpdateTaskRequest request = new UpdateTaskRequest("Titre modifié", "Desc modifiée", TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));

        assertThrows(UnauthorizedAccessException.class, () -> taskService.updateTask(100L, request, 2L));
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("Modifier le statut d'une tâche d'un AUTRE utilisateur doit lever UnauthorizedAccessException (403)")
    void updateTaskStatus_BelongsToAnotherUser_ThrowsUnauthorizedAccessException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));

        assertThrows(UnauthorizedAccessException.class, () -> taskService.updateTaskStatus(100L, request, 2L));
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("Supprimer une tâche appartenant à l'utilisateur connecté")
    void deleteTask_OwnedByUser_Success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));

        taskService.deleteTask(100L, 1L);

        verify(taskRepository, times(1)).delete(taskEntity);
    }

    @Test
    @DisplayName("Supprimer une tâche d'un AUTRE utilisateur doit lever UnauthorizedAccessException (403)")
    void deleteTask_BelongsToAnotherUser_ThrowsUnauthorizedAccessException() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(taskEntity));

        assertThrows(UnauthorizedAccessException.class, () -> taskService.deleteTask(100L, 2L));
        verify(taskRepository, never()).delete(any(TaskEntity.class));
    }
}
