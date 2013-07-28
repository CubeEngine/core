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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.map.hash.THashMap;
import io.netty.handler.codec.http.HttpHeaders;

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
        this.headers = new THashMap<>();
        this.data = data;

        List<String> list;
        for (Map.Entry<String, String> entry : headers)
        {
            list = this.headers.get(entry.getKey());
            if (list == null)
            {
                this.headers.put(entry.getKey(), list = new ArrayList<>(1));
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
