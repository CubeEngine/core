package de.cubeisland.cubeengine.core.webapi;

import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * this class wrapps the data which will be send to the client
 *
 * @since 1.0.0
 */
public final class ApiResponse
{
    private final Map<String, List<String>> headers;
    private Object content;

    /**
     * Initializes the the response
     */
    public ApiResponse()
    {
        this.headers = new HashMap<String, List<String>>();
        this.content = null;
    }

    /**
     * Returns an header
     *
     * @param name the name of the header
     * @return the value of the header
     */
    public List<String> getHeader(String name)
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
        Validate.notNull(name, "name must not be null!");

        if (value == null)
        {
            this.headers.remove(name);
        }
        else
        {
            List<String> values = new LinkedList<String>();
            values.add(value);
            this.headers.put(name, values);
        }
        return this;
    }

    public ApiResponse addHeader(String name, String value)
    {
        Validate.notNull(name, "The name must not be null!");
        Validate.notNull(value, "The value must not be null!");

        List<String> values = this.headers.get(name);
        if (values == null)
        {
            this.headers.put(name, values = new LinkedList<String>());
        }
        values.add(value);

        return this;
    }

    /**
     * Returns a copy of the header map
     *
     * @return the header map
     */
    public Map<String, List<String>> getHeaders()
    {
        return this.headers;
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
    public ApiResponse setHeaders(Map<String, List<String>> headers)
    {
        if (headers != null)
        {
            for (Map.Entry<String, List<String>> header : headers.entrySet())
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
