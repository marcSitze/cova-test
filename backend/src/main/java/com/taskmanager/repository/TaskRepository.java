package com.taskmanager.repository;

import com.taskmanager.entity.TaskEntity;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);

    Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    Optional<TaskEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
