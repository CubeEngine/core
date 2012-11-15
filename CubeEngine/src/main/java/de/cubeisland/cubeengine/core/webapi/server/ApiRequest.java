package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the information of the API request. It is only used
 * to pass the information the to executing action, nothing more.
 */
public final class ApiRequest
{
    private static final String AUTHKEY_PARAM_NAME = "authkey";
    private final InetSocketAddress remoteAddress;
    private final RequestMethod method;
    private final String uri;
    private final String path;
    private final String queryString;
    private final String format;
    private final String authenticationKey;
    private final String controller;
    private final String action;
    private final String userAgent;
    private final boolean ignoreResponseStatus;
    public final Parameters params;
    public final Map<String, String> headers;

    /**
     * Initializes the ApiRequest with an Server instance
     */
    public ApiRequest(final InetSocketAddress remoteAddress, final HttpRequest request)
    {
        int offset;
        this.remoteAddress = remoteAddress;

        String tempUri = request.getUri();
        // we process a maximum of 1024 characters
        this.uri = tempUri.substring(0, Math.min(1024, tempUri.length())).replace(';', '&');

        String tempPath;
        if ((offset = this.uri.indexOf("?")) < 0)
        {
            tempPath = this.uri;
            this.queryString = null;
        }
        else
        {
            tempPath = this.uri.substring(0, offset);
            this.queryString = this.uri.substring(offset + 1);
        }

        if ((offset = tempPath.lastIndexOf(".")) < 0)
        {
            this.format = null;
        }
        else
        {
            this.format = tempPath.substring(offset + 1);
            tempPath = tempPath.substring(0, offset);
        }

        this.path = tempPath;

        Map<String, String> tempParams = new HashMap<String, String>();
        StringUtils.parseQueryString(this.queryString, tempParams);
        final ByteBuf content = request.getContent();
        if (content.readable())
        {
            StringUtils.parseQueryString(content.toString(), tempParams);
        }

        this.authenticationKey = tempParams.get(AUTHKEY_PARAM_NAME);
        tempParams.remove(AUTHKEY_PARAM_NAME);

        this.params = new Parameters(tempParams);

        RequestMethod tempMethod = RequestMethod.getByName(params.get("method"));
        if (tempMethod == null)
        {
            tempMethod = RequestMethod.getByName(request.getMethod().getName());
        }
        if (tempMethod == null)
        {
            tempMethod = RequestMethod.GET;
        }
        this.method = tempMethod;

        final Map<String, String> tempHeaders = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : request.getHeaders())
        {
            tempHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        this.headers = Collections.unmodifiableMap(tempHeaders);
        this.userAgent = this.headers.get("user-agent");

        String route = this.path.substring(1);
        String[] routeSegments = StringUtils.explode("/", route, false);
        if (routeSegments.length >= 2)
        {
            this.controller = routeSegments[0];
            this.action = routeSegments[1];
        }
        else
        {
            if (routeSegments.length >= 1)
            {
                this.controller = routeSegments[0];
                this.action = null;
            }
            else
            {
                this.controller = null;
                this.action = null;
            }
        }

        this.ignoreResponseStatus = this.params.containsKey("ignoreResponseStatus");
    }

    public RequestMethod getMethod()
    {
        return this.method;
    }

    public String getUri()
    {
        return this.uri;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getQueryString()
    {
        return this.queryString;
    }

    public String getFormat()
    {
        return this.format;
    }

    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    public InetSocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }

    public String getController()
    {
        return this.controller;
    }

    public String getAction()
    {
        return this.action;
    }

    public String getUserAgent()
    {
        return this.userAgent;
    }

    public boolean ignoreResponseStatus()
    {
        return this.ignoreResponseStatus;
    }
}