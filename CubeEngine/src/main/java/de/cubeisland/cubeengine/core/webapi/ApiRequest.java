package de.cubeisland.cubeengine.core.webapi;

import gnu.trove.map.hash.THashMap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class contains all the information of the API request. It is only used
 * to pass the information the to executing action, nothing more.
 */
public final class ApiRequest
{
    private final InetSocketAddress remoteAddress;
    private final RequestMethod method;
    private final Parameters params;
    private final Map<String, List<String>> headers;

    /**
     * Initializes the ApiRequest with an Server instance
     */
    public ApiRequest(final InetSocketAddress remoteAddress, RequestMethod method, Parameters params, List<Map.Entry<String, String>> headers)
    {
        this.remoteAddress = remoteAddress;
        this.method = method;
        this.params = params;
        this.headers = new THashMap<String, List<String>>();

        List<String> list;
        for (Map.Entry<String, String> entry : headers)
        {
            list = this.headers.get(entry.getKey());
            if (list == null)
            {
                this.headers.put(entry.getKey(), list = new ArrayList<String>(1));
            }
            list.add(entry.getValue());
        }
    }

    public InetSocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }

    public RequestMethod getMethod()
    {
        return this.method;
    }

    public Parameters getParams()
    {
        return this.params;
    }

    public Map<String, List<String>> getHeaders()
    {
        return this.headers;
    }
}
