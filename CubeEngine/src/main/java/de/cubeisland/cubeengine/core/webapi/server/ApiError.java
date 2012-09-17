package de.cubeisland.cubeengine.core.webapi.server;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * This enum contains the different API errors which get return by the server if
 * something goes wrong while processing the request
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public enum ApiError
{
    UNKNOWN_ERROR(100, HttpResponseStatus.INTERNAL_SERVER_ERROR, "An unhandled exception interrupted the request processing"),
    ACTION_DISABLED(101, HttpResponseStatus.FORBIDDEN, "The requested action is disabled"),
    AUTHENTICATION_FAILURE(200, HttpResponseStatus.UNAUTHORIZED, "Wrong authentication key given"),
    REQUEST_EXCEPTION(201, HttpResponseStatus.BAD_REQUEST, "The called action was not satiesfied by the request"),
    ACTION_NOT_IMPLEMENTED(301, HttpResponseStatus.NOT_IMPLEMENTED, "The called action is not yet implemented"),
    CONTROLLER_NOT_FOUND(202, HttpResponseStatus.NOT_FOUND, "The requested controller was not found"),
    ACTION_NOT_FOUND(203, HttpResponseStatus.NOT_FOUND, "The requested action was not found"),
    METHOD_NOT_ALLOWED(204, HttpResponseStatus.METHOD_NOT_ALLOWED, "The method you used is not allowed here"),
    MISSING_PARAMETERS(204, HttpResponseStatus.BAD_REQUEST, "Not all needed parameters where given");
    private final int errorCode;
    private final HttpResponseStatus responseStatus;
    private final String description;

    /**
     * initializes the ApiError with an error code and an HttpResponseStatus
     */
    private ApiError(int errorCode, HttpResponseStatus responseStatus, String description)
    {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.description = description;
    }

    /**
     * Returns the error code of the ApiError instance
     *
     * @return the code
     */
    public int getCode()
    {
        return this.errorCode;
    }

    /**
     * Returns the HttpResponseStatus of the ApiError instance
     *
     * @return the response status
     */
    public HttpResponseStatus getRepsonseStatus()
    {
        return this.responseStatus;
    }

    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.errorCode);
    }
}