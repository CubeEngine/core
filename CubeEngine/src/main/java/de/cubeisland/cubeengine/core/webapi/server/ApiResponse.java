package de.cubeisland.cubeengine.core.webapi.server;

import java.util.HashMap;
import java.util.Map;

/**
 * this class wrapps the data which will be send to the client
 *
 * @since 1.0.0
 */
public final class ApiResponse
{
    private final Map<String, String> headers;
    private Object content;

    /**
     * Initlaizes the the response with a default serializer
     *
     * @param serializer the serializer instance
     */
    public ApiResponse()
    {
        this.headers = new HashMap<String, String>();
        this.content = null;
    }

    /**
     * Returns an header
     *
     * @param name the name of the header
     * @return the value of the header
     */
    public String getHeader(String name)
    {
        if (name != null)
        {
            return this.headers.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Sets an header
     *
     * @param name  the name of the param
     * @param value the value of the header
     * @return fluent interface
     */
    public ApiResponse setHeader(String name, String value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name must not be null!");
        }
        if (value == null)
        {
            this.headers.remove(name);
        }
        else
        {
            this.headers.put(name, value);
        }
        return this;
    }

    /**
     * Returns a copy of the header map
     *
     * @return the header map
     */
    public Map<String, String> getHeaders()
    {
        return new HashMap<String, String>(this.headers);
    }

    /**
     * Clears the headers
     *
     * @return fluent interface
     */
    public ApiResponse clearHeaders()
    {
        this.headers.clear();
        return this;
    }

    /**
     * Sets the whole header map
     *
     * @param headers the header map
     * @return fluent interface
     */
    public ApiResponse setHeaders(Map<String, String> headers)
    {
        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                this.headers.put(header.getKey().toLowerCase(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Returns the content of the response
     *
     * @return the response object
     */
    public Object getContent()
    {
        return this.content;
    }

    /**
     * Sets the response content#
     *
     * @param content an object which will per serialized
     * @return fluent interface
     */
    public ApiResponse setContent(Object content)
    {
        this.content = content;
        return this;
    }
}