package by.edu.example.taskmanager.dto;

import java.time.Instant;

/** Единый формат сообщения об ошибке REST API. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message) {
}
