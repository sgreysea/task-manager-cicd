package by.edu.example.taskmanager.exception;

/** Исключение предметной области для отсутствующей задачи. */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(long id) {
        super("Задача с идентификатором " + id + " не найдена");
    }
}
