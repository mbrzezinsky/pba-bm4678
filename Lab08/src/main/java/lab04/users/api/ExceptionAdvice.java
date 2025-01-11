package lab04.users.api;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lab04.users.api.model.Error;

@ControllerAdvice
public class ExceptionAdvice
{

    @ExceptionHandler
    ResponseEntity<Error> handle(Throwable ex)
    {
        return new ResponseEntity<>(
                new Error().code("400").message(ex.getMessage()),
                HttpStatusCode.valueOf(400)
        );
    }
}
