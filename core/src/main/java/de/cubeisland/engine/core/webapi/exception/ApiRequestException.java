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
package de.cubeisland.engine.core.webapi.exception;

import java.util.HashMap;

/**
 * This exception should be used to express an error during the action
 * execution. For example when the action expects a number as parameter, but a
 * letter was given.
 *
 * @since 1.0.0
 */
public class ApiRequestException extends RuntimeException
{
    private int code;

    /**
     * Initializes the exception with a message and a reason. The given message
     * won't be send to the client, it's just used to inform the console
     *
     * @param msg  the message
     * @param code the reason for this error
     */
    public ApiRequestException(String msg, int code)
    {
        super(msg);
        this.code = code;
    }

    /**
     * Initializes the exception with a message, a reason and the cause of the
     * error The given message won't be send to the client, it's just used to
     * inform the console
     *
     * @param message the message
     * @param code    the reason for this error
     * @param cause   the cause of te error
     */
    public ApiRequestException(String message, int code, Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    /**
     * Returns the error code
     *
     * @return the error code
     */
    public int getCode()
    {
        return this.code;
    }

    @Override
    public String toString()
    {
        return this.getMessage();
    }

    /**
     * TODO switch to converter
     */
    public Object serialize()
    {
        HashMap<String, Object> data = new HashMap<>(2);
        data.put("code", this.code);
        data.put("message", this.getMessage());
        return data;
    }
}
