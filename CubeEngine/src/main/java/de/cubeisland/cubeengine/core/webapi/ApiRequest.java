package de.cubeisland.cubeengine.core.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.map.hash.THashMap;
import io.netty.handler.codec.http.HttpHeaders;

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
    private final Parameters urlParams;
    private final Map<String, List<String>> headers;
    private final JsonNode data;

    /**
     * Initializes the ApiRequest with an Server instance
     */
    public ApiRequest(final InetSocketAddress remoteAddress, RequestMethod method, Parameters params, HttpHeaders headers, JsonNode data)
    {
        this.remoteAddress = remoteAddress;
        this.method = method;
        this.urlParams = params;
        this.headers = new THashMap<String, List<String>>();
        this.data = data;

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
        return this.urlParams;
    }

    public Map<String, List<String>> getHeaders()
    {
        return this.headers;
    }

    public JsonNode getData()
    {
        return this.data;
    }
}
