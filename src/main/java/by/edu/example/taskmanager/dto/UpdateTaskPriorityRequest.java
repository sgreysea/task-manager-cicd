package by.edu.example.taskmanager.dto;

import by.edu.example.taskmanager.model.TaskPriority;
import jakarta.validation.constraints.NotNull;

/** DTO для изменения приоритета уже существующей задачи. */
public record UpdateTaskPriorityRequest(
        @NotNull(message = "Приоритет задачи обязателен")
        TaskPriority priority) {
}
