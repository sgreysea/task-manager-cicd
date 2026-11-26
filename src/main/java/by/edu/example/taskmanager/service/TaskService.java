package by.edu.example.taskmanager.service;

import by.edu.example.taskmanager.exception.TaskNotFoundException;
import by.edu.example.taskmanager.model.Task;
import by.edu.example.taskmanager.model.TaskPriority;
import by.edu.example.taskmanager.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Учебный сервис задач без внешней базы данных.
 * ConcurrentHashMap обеспечивает потокобезопасные операции над хранилищем,
 * AtomicLong выдаёт уникальные ID внутри одного экземпляра приложения.
 */
@Service
public class TaskService {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);

    /** Старый контракт оставлен для совместимости: приоритет по умолчанию NORMAL. */
    public Task create(String title, String description) {
        return create(title, description, TaskPriority.NORMAL);
    }

    /** Создаёт задачу с указанным приоритетом. null трактуется как NORMAL. */
    public Task create(String title, String description, TaskPriority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название задачи не должно быть пустым");
        }
        String normalizedTitle = title.trim();
        String normalizedDescription = description == null ? "" : description.trim();
        TaskPriority effectivePriority = priority == null ? TaskPriority.NORMAL : priority;
        long id = nextId.incrementAndGet();
        Task task = new Task(id, normalizedTitle, normalizedDescription,
                TaskStatus.NEW, effectivePriority, Instant.now());
        tasks.put(id, task);
        return task;
    }

    /** Возвращает все задачи в порядке возрастания ID. */
    public List<Task> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    /** Возвращает только задачи заданного приоритета. */
    public List<Task> findByPriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Приоритет для фильтрации обязателен");
        }
        return tasks.values().stream()
                .filter(task -> task.priority() == priority)
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    public Task findById(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    /** Изменение статуса сохраняет приоритет и остальные поля задачи. */
    public Task changeStatus(long id, TaskStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Статус задачи обязателен");
        }
        return tasks.compute(id, (key, current) -> {
            if (current == null) {
                throw new TaskNotFoundException(id);
            }
            return new Task(current.id(), current.title(), current.description(),
                    newStatus, current.priority(), current.createdAt());
        });
    }

    /** Изменение приоритета сохраняет статус и остальные поля задачи. */
    public Task changePriority(long id, TaskPriority newPriority) {
        if (newPriority == null) {
            throw new IllegalArgumentException("Приоритет задачи обязателен");
        }
        return tasks.compute(id, (key, current) -> {
            if (current == null) {
                throw new TaskNotFoundException(id);
            }
            return new Task(current.id(), current.title(), current.description(),
                    current.status(), newPriority, current.createdAt());
        });
    }

    public void delete(long id) {
        // remove(id) возвращает удалённый объект или null. Проверка и удаление
        // выполняются одной атомарной операцией для ключа; повторное удаление даст 404.
        Task removed = tasks.remove(id);
        if (removed == null) {
            throw new TaskNotFoundException(id);
        }
    }
}
