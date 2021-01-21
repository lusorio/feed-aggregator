package com.assignment.aggregator.exceptions.exceptionhandler;

import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.exceptions.InvalidChannelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * Support for custom exception handling at {@link java.lang.ModuleLayer.Controller} level. Mimics
 * the default response given by Spring for 5xx errors. i.e:
 *
 * <pre><code*
 * {
 *     "timestamp":"2019-02-27T04:03:52.398+0000",
 *     "status":500,
 *     "error":"Internal Server Error",
 *     "message":"...",
 *     "path":"/path"
 * }
 * </pre>
 */
@ControllerAdvice
class ControllerExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomErrorResponse> constraintViolationExceptionHandler(Exception ex, WebRequest request)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Exception catch in ControllerExceptionHandler: ConstraintViolationException");
            logger.info(MessageFormat.format("Exception is: {0}", ex.getMessage()));
        }

        return createErrorResponse(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> notFoundExceptionHandler(Exception ex, WebRequest request)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Exception catch in ControllerExceptionHandler: ChannelNotFoundException");
            logger.info(MessageFormat.format("Exception is: {0}", ex.getMessage()));
        }

        return createErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicatedChannelException.class)
    public ResponseEntity<CustomErrorResponse> duplicatedEntityExceptionHandler(Exception ex, WebRequest request)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Exception catch in DuplicatedChannelException: ChannelNotFoundException");
            logger.info(MessageFormat.format("Exception is: {0}", ex.getMessage()));
        }

        return createErrorResponse(request, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidChannelException.class)
    public ResponseEntity<CustomErrorResponse> unreachableFeedExceptionHandler(Exception ex, WebRequest request)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Exception catch in InvalidChannelException: ChannelNotFoundException");
            logger.info(MessageFormat.format("Exception is: {0}", ex.getMessage()));
        }

        return createErrorResponse(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<CustomErrorResponse> createErrorResponse(WebRequest request, HttpStatus status, String message)
    {
        var servletRequest = ((ServletWebRequest) request).getRequest();

        var error = new CustomErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setMessage(message);
        error.setError(status.getReasonPhrase());
        error.setStatus(status.value());
        error.setHttpMethod(servletRequest.getMethod());
        error.setPath(servletRequest.getRequestURI());

        if (logger.isInfoEnabled())
        {
            logger.info("Created custom error response:");
            logger.info(error.toString());
        }

        return new ResponseEntity<>(error, status);
    }
}