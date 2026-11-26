package by.edu.example.taskmanager.model;

import java.time.Instant;

/**
 * Неизменяемая модель задачи.
 * Поле priority добавлено в рамках индивидуального задания № 1.
 */
public record Task(
        long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt) {
}
