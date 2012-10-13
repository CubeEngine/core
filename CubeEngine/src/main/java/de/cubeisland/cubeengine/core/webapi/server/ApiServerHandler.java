package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.webapi.server.exception.ApiNotImplementedException;
import de.cubeisland.cubeengine.core.webapi.server.exception.ApiRequestException;
import de.cubeisland.cubeengine.core.webapi.server.exception.UnauthorizedRequestException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiServerHandler extends SimpleChannelUpstreamHandler
{
    private final static ApiServer server = ApiServer.getInstance();
    private final static ApiManager manager = ApiManager.getInstance();
    private ApiRequest request;

    @Override
    public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event)
    {
        // ApiBukkit.logException(event.getCause()); -- TODO fix logging
        context.getChannel().write(toResponse(ApiError.UNKNOWN_ERROR)).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent message) throws Exception
    {
        final InetSocketAddress remoteAddress = (InetSocketAddress)message.getRemoteAddress();
        if (manager.isBlacklisted(remoteAddress) || !manager.isWhitelisted(remoteAddress))
        {
            message.getChannel().close();
            return;
        }

        message.getFuture().addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        request = new ApiRequest(remoteAddress, (HttpRequest)message.getMessage());
        HttpResponse response = this.processRequest(request);

        ChannelFuture future = message.getChannel().write(response);

        future.addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse processRequest(ApiRequest request)
    {
        // ApiBukkit.log(String.format("'%s' requested '%s'", request.getRemoteAddress().getAddress().getHostAddress(), request.getPath()), ApiLogLevel.INFO); -- TODO fix logging
        String useragent = request.headers.get("user-agent");
        if (useragent != null)
        {
            // ApiBukkit.log("Useragent: " + useragent, ApiLogLevel.INFO); -- TODO fix logging
        }

        String controllerName = request.getController();
        String actionName = request.getAction();

//            if (actionName != null)
//            {
//                debug("Controller: " + controllerName);
//            }
//            if (actionName != null)
//            {
//                debug("Action: " + actionName);
//            }

        ApiController controller = manager.getController(controllerName);
        ApiResponse response = new ApiResponse(manager.getDefaultSerializer());
        if (controller != null)
        {
            // debug("Controller found: " + controller.getClass().getName()); -- TODO fix logging

            try
            {
                if (actionName != null)
                {
                    ApiAction action = controller.getAction(actionName);
                    if (manager.isActionDisabled(controllerName, actionName))
                    {
                        // ApiBukkit.error("Requested action is disabled!"); -- TODO fix logging
                        return toResponse(ApiError.ACTION_DISABLED);
                    }
                    if (action != null)
                    {
                        authorized(request, action);

                        for (String param : action.getParameters())
                        {
                            if (!request.params.containsKey(param))
                            {
                                // ApiBukkit.error("Request had to few arguments!"); -- TODO fix logging
                                return toResponse(ApiError.MISSING_PARAMETERS);
                            }
                        }

                        response.setSerializer(getSerializer(request, action.getSerializer()));
                        // ApiBukkit.debug("Action found: " + actionName); -- TODO fix logging
                        action.execute(request, response);
                    }
                    else
                    {
                        if (controller.isUnknownToDefaultRoutingAllowed())
                        {
                            authorized(request, controller);

                            response.setSerializer(getSerializer(request, controller.getSerializer()));
                            // ApiBukkit.debug("action not found, routing to default action"); -- TODO fix logging
                            controller.defaultAction(request, response);
                        }
                        else
                        {
                            // ApiBukkit.log("Action not found"); -- TODO fix logging
                            return toResponse(ApiError.ACTION_NOT_FOUND);
                        }
                    }
                }
                else
                {
                    authorized(request, controller);

                    response.setSerializer(getSerializer(request, controller.getSerializer()));
                    // ApiBukkit.debug("Runnung default action"); -- TODO fix logging
                    controller.defaultAction(request, response);
                }
            }
            catch (UnauthorizedRequestException e)
            {
                // ApiBukkit.error("Wrong authentication key!"); -- TODO fix logging
                return toResponse(ApiError.AUTHENTICATION_FAILURE);
            }
            catch (ApiRequestException e)
            {
                // ApiBukkit.error("ControllerException: " + e.getMessage()); -- TODO fix logging
                return toResponse(ApiError.REQUEST_EXCEPTION, e);
            }
            catch (ApiNotImplementedException e)
            {
                // ApiBukkit.error("action not implemented"); -- TODO fix logging
                return toResponse(ApiError.ACTION_NOT_IMPLEMENTED);
            }
            catch (Throwable t)
            {
                // ApiBukkit.logException(t); -- TODO fix logging
                return toResponse(ApiError.UNKNOWN_ERROR);
            }
        }
        else
        {
            // ApiBukkit.log("Controller not found!"); -- TODO fix logging
            return toResponse(ApiError.CONTROLLER_NOT_FOUND);
        }

        return toResponse(response);
    }

    private static void authorized(ApiRequest request, ApiController controller)
    {
        // ApiBukkit.debug("Authkey: " + request.getAuthenticationKey()); -- TODO fix logging
        if (controller.isAuthNeeded() && !server.getAuthenticationKey().equals(request.getAuthenticationKey()))
        {
            throw new UnauthorizedRequestException();
        }
    }

    private static void authorized(ApiRequest request, ApiAction action)
    {
        // ApiBukkit.debug("Authkey: " + request.getAuthenticationKey()); -- TODO fix logging
        if (action.isAuthNeeded() && !server.getAuthenticationKey().equals(request.getAuthenticationKey()))
        {
            throw new UnauthorizedRequestException();
        }
    }

    /**
     * This methods determines the response serializer to use
     *
     * Order: format-parameter action serializer default serializer
     *
     * @param request
     * @param def
     * @return
     */
    private ApiResponseSerializer getSerializer(ApiRequest request, String acionSerializer)
    {
        ApiResponseSerializer serializer = manager.getSerializer(request.getFormat());

        if (serializer == null)
        {
            serializer = manager.getSerializer(acionSerializer);
        }

        if (serializer == null)
        {
            serializer = manager.getDefaultSerializer();
        }
        return serializer;
    }

    private HttpResponse toResponse(ApiResponse response)
    {
        Object content = response.getContent();
        HttpResponseStatus status = HttpResponseStatus.OK;
        if (content == null)
        {
            if (request.ignoreResponseStatus())
            {
                Map<String, Object> data = new HashMap<String, Object>(1);

                data.put("status", HttpResponseStatus.NO_CONTENT.getCode());
                data.put("description", "The request was successful, but there was nothing to return");

                content = data;
            }
            else
            {
                status = HttpResponseStatus.NO_CONTENT;
            }
        }

        return toResponse(status, response.getHeaders(), response.getSerializer(), content);
    }

    private HttpResponse toResponse(HttpVersion version, HttpResponseStatus status, Map<String, String> headers, final String content)
    {
        HttpResponse response = new DefaultHttpResponse(version, status);
        response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        for (Entry<String, String> header : headers.entrySet())
        {
            response.setHeader(header.getKey(), header.getValue());
        }

        return response;
    }

    private HttpResponse toResponse(HttpResponseStatus status, Map<String, String> headers, String content)
    {
        return toResponse(HttpVersion.HTTP_1_1, status, headers, content);
    }

    private HttpResponse toResponse(HttpResponseStatus status, Map<String, String> headers, ApiResponseSerializer serializer, Object contentObject)
    {
        String contentString = serializer.serialize(contentObject);

        headers.put("Content-Length", String.valueOf(contentString.length()));
        headers.put("Content-Type", serializer.getMime().toString());

        return toResponse(status, headers, contentString);
    }

    private HttpResponse toResponse(ApiError error)
    {
        return toResponse(error, null);
    }

    private HttpResponse toResponse(ApiError error, ApiRequestException cause)
    {
        HttpResponseStatus status = error.getRepsonseStatus();
        Map data = new HashMap();
        if (request.ignoreResponseStatus())
        {
            status = HttpResponseStatus.OK;
            data.put("status", error.getRepsonseStatus().getCode());
        }
        data.put("error", error.getCode());
        data.put("description", error.getDescription());

        if (cause != null)
        {
            data.put("cause", cause);
        }

        return toResponse(status, new HashMap<String, String>(2), manager.getDefaultSerializer(), data);
    }
}