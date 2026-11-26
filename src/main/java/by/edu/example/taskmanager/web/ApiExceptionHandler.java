package by.edu.example.taskmanager.web;

import by.edu.example.taskmanager.dto.ApiError;
import by.edu.example.taskmanager.exception.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Преобразует ожидаемые ошибки приложения и некорректный ввод в единый JSON ApiError. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TaskNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Входные данные не прошли проверку");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /** Ошибка преобразования query/path-параметра, например priority=URGENT. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = "Недопустимое значение параметра '" + exception.getName() + "'";
        Class<?> requiredType = exception.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String allowed = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message += ". Допустимые значения: " + allowed;
        }
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /** Ошибка JSON-десериализации, в том числе неизвестное значение enum в теле запроса. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST,
                "Некорректное тело запроса: проверьте JSON и допустимые значения полей");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError body = new ApiError(Instant.now(), status.value(),
                status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}
