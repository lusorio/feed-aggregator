package com.assignment.aggregator.exceptions.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
public class CustomErrorResponse
{

    /**
     * the timestamp when the {@link Exception} occur
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime timestamp;

    /**
     * The {@link org.springframework.http.HttpStatus} error code
     */
    private int status;

    /**
     * The exception title. i.e: Internal Server Error, Not Found, Forbidden.
     */
    private String error;

    /**
     * The actual {@link Exception} message
     */
    private String message;

    /**
     * The {@link org.springframework.http.HttpMethod} that originated the exception (GET, POST, PUT, DELETE,...)
     */
    private String httpMethod;

    /**
     * The API path at which the {@link Exception} was thrown
     */
    private String path;

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public CustomErrorResponse setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

    public int getStatus()
    {
        return status;
    }

    public CustomErrorResponse setStatus(int status)
    {
        this.status = status;
        return this;
    }

    public String getError()
    {
        return error;
    }

    public CustomErrorResponse setError(String error)
    {
        this.error = error;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public CustomErrorResponse setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public CustomErrorResponse setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
        return this;
    }

    public String getPath()
    {
        return path;
    }

    public CustomErrorResponse setPath(String path)
    {
        this.path = path;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CustomErrorResponse that = (CustomErrorResponse) o;

        return new EqualsBuilder()
                       .append(getStatus(), that.getStatus())
                       .append(getTimestamp(), that.getTimestamp())
                       .append(getError(), that.getError())
                       .append(getMessage(), that.getMessage())
                       .append(getHttpMethod(), that.getHttpMethod())
                       .append(getPath(), that.getPath())
                       .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                       .append(getTimestamp())
                       .append(getStatus())
                       .append(getError())
                       .append(getMessage())
                       .append(getHttpMethod())
                       .append(getPath())
                       .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                       .append("timestamp", timestamp)
                       .append("status", status)
                       .append("error", error)
                       .append("message", message)
                       .append("httpMethod", httpMethod)
                       .append("path", path)
                       .toString();
    }
}