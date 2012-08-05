package de.cubeisland.cubeengine.core.webapi.server;

import java.util.HashMap;

/**
 * A list of supported request methods
 *
 * @author Phillip Schichtel
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
        BY_NAME = new HashMap<String, RequestMethod>(values().length);

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
        return BY_NAME.get(name.toUpperCase());
    }
}
