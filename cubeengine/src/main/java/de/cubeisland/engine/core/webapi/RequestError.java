/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.webapi;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * This enum contains the different API errors which get return by the server if
 * something goes wrong while processing the request
 */
public enum RequestError
{
    UNKNOWN_ERROR(
        100,
        HttpResponseStatus.INTERNAL_SERVER_ERROR,
        "An unhandled exception interrupted the request processing"),
    ACTION_DISABLED(
        101,
        HttpResponseStatus.FORBIDDEN,
        "The requested action is disabled"),
    AUTHENTICATION_FAILURE(
        200,
        HttpResponseStatus.UNAUTHORIZED,
        "Wrong authentication key given"),
    REQUEST_EXCEPTION(
        201,
        HttpResponseStatus.BAD_REQUEST,
        "The called action was not satisfied by the request"),
    ROUTE_NOT_FOUND(
        202,
        HttpResponseStatus.NOT_FOUND,
        "The requested action was not found"),
    METHOD_NOT_ALLOWED(
        203,
        HttpResponseStatus.METHOD_NOT_ALLOWED,
        "The method you used is not allowed here"),
    MISSING_PARAMETERS(
        204,
        HttpResponseStatus.BAD_REQUEST,
        "Not all needed parameters where given"),
    MALFORMED_DATA(
        205,
        HttpResponseStatus.BAD_REQUEST,
        "The request body could not be interpreted as valid JSON code"),
    HANDLER_NOT_IMPLEMENTED(
        301,
        HttpResponseStatus.NOT_IMPLEMENTED,
        "The called action is not yet implemented");
    private final int errorCode;
    private final HttpResponseStatus responseStatus;
    private final String description;

    /**
     * initializes the RequestError with an error code and an HttpResponseStatus
     */
    private RequestError(int errorCode, HttpResponseStatus responseStatus, String description)
    {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.description = description;
    }

    /**
     * Returns the error code of the RequestError instance
     *
     * @return the code
     */
    public int getCode()
    {
        return this.errorCode;
    }

    /**
     * Returns the HttpResponseStatus of the RequestError instance
     *
     * @return the response status
     */
    HttpResponseStatus getRepsonseStatus()
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
