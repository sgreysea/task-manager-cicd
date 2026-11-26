package by.edu.example.taskmanager.dto;

import by.edu.example.taskmanager.model.TaskPriority;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

/** DTO для создания задачи. Если priority не передан, сервис назначает NORMAL. */
public record CreateTaskRequest(
        @NotBlank(message = "Название задачи не должно быть пустым")
        @Size(max = 120, message = "Название не должно превышать 120 символов")
        String title,

        @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
        String description,

        // Поле необязательно: отсутствие или null означает NORMAL.
        TaskPriority priority) {

    /**
     * Совместимый конструктор для существующих вызовов базового проекта.
     * Благодаря ему старые тесты и клиенты Java-кода не требуют массовой правки.
     */
    public CreateTaskRequest(String title, String description) {
        this(title, description, null);
    }
}
