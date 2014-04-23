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

import java.util.HashMap;
import java.util.Locale;

/**
 * A list of supported request methods.
 */
public enum RequestMethod
{
    GET,
    POST,
    PUT,
    DELETE,
    OPTIONS,
    HEAD,
    PATCH,
    TRACE,
    CONNECT;
    private static final HashMap<String, RequestMethod> BY_NAME;

    static
    {
        BY_NAME = new HashMap<>(values().length);

        for (RequestMethod method : values())
        {
            BY_NAME.put(method.name(), method);
        }
    }

    /**
     * Returns a request method by its name
     *
     * @param name the name of the method (will be normalized)
     * @return the request method or null
     */
    public static RequestMethod getByName(String name)
    {
        if (name == null)
        {
            return null;
        }
        return BY_NAME.get(name.toUpperCase(Locale.ENGLISH));
    }
}
