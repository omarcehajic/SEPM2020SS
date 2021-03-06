package at.ac.tuwien.sepm.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepm.groupphase.backend.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidationException;
import java.lang.invoke.MethodHandles;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register all your Java exceptions here to map them into meaningful HTTP exceptions
 * If you have special cases which are only important for specific endpoints, use ResponseStatusExceptions
 * https://www.baeldung.com/exception-handling-for-rest-with-spring#responsestatusexception
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Use the @ExceptionHandler annotation to write handler for custom exceptions
     */
    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {DeletionException.class})
    protected ResponseEntity<Object> handleCantDelete(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {NotFreeException.class})
    protected ResponseEntity<Object> handleNotFree(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleNotValid(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(value = {AlreadyExistsException.class})
    protected ResponseEntity<Object> handleAlreadyExists(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {IncorrectTypeException.class})
    protected ResponseEntity<Object> handleIncorrectType(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(value = {InvalidDatabaseStateException.class})
    protected ResponseEntity<Object> handleInvalidDatabaseState(RuntimeException ex, WebRequest request) {
        LOGGER.error(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = NotAuthorisedException.class)
    protected ResponseEntity<Object> handleNotAuthorised(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }


    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized HTTP response for a know exception
     * from e.g. Spring
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        //Get all errors
//        List<String> errors = ex.getBindingResult()
//            .getFieldErrors()
//            .stream()
//            .map(err -> err.getField() + " " + err.getDefaultMessage())
//            .collect(Collectors.toList());

        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());

        StringBuilder bodyString = new StringBuilder("");
        if(errors.size() > 1) {
            bodyString.append( "<p>Following Problems occurred:</p>");
            int i = 1;
            for(String e : errors) {
                bodyString.append("<p>").append(i).append(". ")
                    .append(e).append("</p>");
                i++;
            }
        } else if(errors.size() == 1) {
            for(String e : errors) {
                bodyString.append(e);
            }
        }


        body.put("Validation errors", errors);

//        return new ResponseEntity<>(body.toString(), headers, status);
        return new ResponseEntity<>(bodyString, headers, status);
    }
}
