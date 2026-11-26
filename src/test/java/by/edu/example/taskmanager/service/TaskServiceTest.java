package by.edu.example.taskmanager.service;

import by.edu.example.taskmanager.exception.TaskNotFoundException;
import by.edu.example.taskmanager.model.Task;
import by.edu.example.taskmanager.model.TaskPriority;
import by.edu.example.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Модульные тесты логики задач без запуска Spring-контекста. */
class TaskServiceTest {

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService();
    }

    @Test
    void createAssignsIdNormalizesTextAndDefaultsPriority() {
        Task created = service.create("  Prepare CI lab  ", "  Maven and JUnit  ");

        assertEquals(1L, created.id());
        assertEquals("Prepare CI lab", created.title());
        assertEquals("Maven and JUnit", created.description());
        assertEquals(TaskStatus.NEW, created.status());
        assertEquals(TaskPriority.NORMAL, created.priority());
        assertNotNull(created.createdAt());
    }

    @Test
    void createAcceptsEachPriority() {
        for (TaskPriority priority : TaskPriority.values()) {
            Task created = service.create("Task " + priority, "", priority);
            assertEquals(priority, created.priority());
        }
    }

    @Test
    void createTreatsNullPriorityAsNormal() {
        assertEquals(TaskPriority.NORMAL,
                service.create("No explicit priority", "", null).priority());
    }

    @Test
    void createRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create("   ", "description"));
    }

    @Test
    void findAllReturnsTasksOrderedById() {
        Task first = service.create("First", "");
        Task second = service.create("Second", "");

        List<Task> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals(first.id(), result.get(0).id());
        assertEquals(second.id(), result.get(1).id());
    }

    @Test
    void findByPriorityReturnsOnlyMatchingTasksInIdOrder() {
        Task low = service.create("Low", "", TaskPriority.LOW);
        service.create("High", "", TaskPriority.HIGH);
        Task low2 = service.create("Low 2", "", TaskPriority.LOW);

        List<Task> result = service.findByPriority(TaskPriority.LOW);

        assertEquals(List.of(low.id(), low2.id()), result.stream().map(Task::id).toList());
        assertTrue(result.stream().allMatch(task -> task.priority() == TaskPriority.LOW));
    }

    @Test
    void findByPriorityReturnsEmptyListWhenNoTasksMatch() {
        service.create("Normal", "", TaskPriority.NORMAL);

        assertTrue(service.findByPriority(TaskPriority.HIGH).isEmpty());
    }

    @Test
    void findByPriorityRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.findByPriority(null));
    }

    @Test
    void findByIdThrowsForUnknownTask() {
        assertThrows(TaskNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void changeStatusPreservesPriority() {
        Task created = service.create("Review report", "", TaskPriority.HIGH);

        Task updated = service.changeStatus(created.id(), TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, updated.status());
        assertEquals(TaskPriority.HIGH, updated.priority());
        assertEquals(created.title(), updated.title());
        assertEquals(created.createdAt(), updated.createdAt());
    }

    @Test
    void changePriorityPreservesStatusAndOtherFields() {
        Task created = service.create("Release", "Important", TaskPriority.LOW);
        service.changeStatus(created.id(), TaskStatus.IN_PROGRESS);
        Task beforePriorityChange = service.findById(created.id());

        Task updated = service.changePriority(created.id(), TaskPriority.HIGH);

        assertEquals(TaskPriority.HIGH, updated.priority());
        assertEquals(TaskStatus.IN_PROGRESS, updated.status());
        assertEquals(beforePriorityChange.title(), updated.title());
        assertEquals(beforePriorityChange.description(), updated.description());
        assertEquals(beforePriorityChange.createdAt(), updated.createdAt());
    }

    @Test
    void changePriorityRejectsNull() {
        Task created = service.create("Task", "");

        assertThrows(IllegalArgumentException.class,
                () -> service.changePriority(created.id(), null));
    }

    @Test
    void changePriorityThrowsForUnknownTask() {
        assertThrows(TaskNotFoundException.class,
                () -> service.changePriority(999L, TaskPriority.HIGH));
    }

    @Test
    void deleteRemovesExistingTask() {
        Task created = service.create("Delete me", "");

        service.delete(created.id());

        assertTrue(service.findAll().isEmpty());
        assertThrows(TaskNotFoundException.class, () -> service.findById(created.id()));
    }
}
