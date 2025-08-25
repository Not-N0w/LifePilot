package com.github.not.n0w.lifepilot.repository;

import com.github.not.n0w.lifepilot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}