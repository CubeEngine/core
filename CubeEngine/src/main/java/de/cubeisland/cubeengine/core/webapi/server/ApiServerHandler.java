package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.webapi.server.exception.ApiNotImplementedException;
import de.cubeisland.cubeengine.core.webapi.server.exception.ApiRequestException;
import de.cubeisland.cubeengine.core.webapi.server.exception.UnauthorizedRequestException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.*;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiServerHandler extends ChannelInboundMessageHandlerAdapter<Object>
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private final static ApiServer server = ApiServer.getInstance();
    private final static ApiManager manager = ApiManager.getInstance();
    private ApiRequest request;

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable t)
    {
        LOGGER.log(ERROR, t.getLocalizedMessage(), t);
        errorResponse(context, ApiError.UNKNOWN_ERROR);
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext context, Object message) throws Exception
    {
        HttpRequest httpRequest = (HttpRequest)message;
        if (httpRequest.getDecoderResult().isFailure())
        {
            LOGGER.log(ERROR, "The decoder failed on this request!");
            errorResponse(context, ApiError.UNKNOWN_ERROR);
            return;
        }
        
        final InetSocketAddress remoteAddress = (InetSocketAddress)context.channel().remoteAddress();
        if (manager.isBlacklisted(remoteAddress) || !manager.isWhitelisted(remoteAddress))
        {
            context.close();
            return;
        }

        context.channel().newFuture().addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        request = new ApiRequest(remoteAddress, httpRequest);
        HttpResponse response = this.processRequest(request);

        ChannelFuture future = context.write(response);

        future.addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse processRequest(ApiRequest request)
    {
        LOGGER.log(INFO, String.format("'%s' requested '%s'", request.getRemoteAddress().getAddress().getHostAddress(), request.getPath()));
        String useragent = request.headers.get("user-agent");
        if (useragent != null)
        {
            LOGGER.log(INFO, "Useragent: {0}", useragent);
        }

        final String controllerName = request.getController();
        final String actionName = request.getAction();

        ApiController controller = manager.getController(controllerName);
        ApiResponse response = new ApiResponse(manager.getDefaultSerializer());
        if (controller != null)
        {
            LOGGER.log(DEBUG, "Controller found: {0}", controller.getClass().getName());

            try
            {
                if (actionName != null)
                {
                    ApiAction action = controller.getAction(actionName);
                    if (manager.isActionDisabled(controllerName, actionName))
                    {
                        LOGGER.log(ERROR, "Requested action is disabled!");
                        return toResponse(ApiError.ACTION_DISABLED);
                    }
                    if (action != null)
                    {
                        authorized(request, action);

                        for (String param : action.getParameters())
                        {
                            if (!request.params.containsKey(param))
                            {
                                LOGGER.log(ERROR, "Request had to few arguments!");
                                return toResponse(ApiError.MISSING_PARAMETERS);
                            }
                        }

                        response.setSerializer(getSerializer(request, action.getSerializer()));
                        LOGGER.log(DEBUG, "Action found: {0}", actionName);
                        action.execute(request, response);
                    }
                    else
                    {
                        if (controller.isUnknownToDefaultRoutingAllowed())
                        {
                            authorized(request, controller);

                            response.setSerializer(getSerializer(request, controller.getSerializer()));
                            LOGGER.log(DEBUG, "action not found, routing to default action");
                            controller.defaultAction(request, response);
                        }
                        else
                        {
                            LOGGER.log(NOTICE, "Action not found");
                            return toResponse(ApiError.ACTION_NOT_FOUND);
                        }
                    }
                }
                else
                {
                    authorized(request, controller);

                    response.setSerializer(getSerializer(request, controller.getSerializer()));
                    LOGGER.log(DEBUG, "Runnung default action");
                    controller.defaultAction(request, response);
                }
            }
            catch (UnauthorizedRequestException e)
            {
                LOGGER.log(ERROR, "Wrong authentication key!");
                return toResponse(ApiError.AUTHENTICATION_FAILURE);
            }
            catch (ApiRequestException e)
            {
                LOGGER.log(ERROR, "ControllerException: {0}", e.getLocalizedMessage());
                return toResponse(ApiError.REQUEST_EXCEPTION, e);
            }
            catch (ApiNotImplementedException e)
            {
                LOGGER.log(ERROR, "action not implemented");
                return toResponse(ApiError.ACTION_NOT_IMPLEMENTED);
            }
            catch (Throwable t)
            {
                LOGGER.log(ERROR, t.getLocalizedMessage(), t);
                return toResponse(ApiError.UNKNOWN_ERROR);
            }
        }
        else
        {
            LOGGER.log(NOTICE, "Controller not found!");
            return toResponse(ApiError.CONTROLLER_NOT_FOUND);
        }

        return toResponse(response);
    }

    private static void authorized(ApiRequest request, ApiController controller)
    {
        LOGGER.log(DEBUG, "Authkey: {0}", request.getAuthenticationKey());
        if (controller.isAuthNeeded() && !server.getAuthenticationKey().equals(request.getAuthenticationKey()))
        {
            throw new UnauthorizedRequestException();
        }
    }

    private static void authorized(ApiRequest request, ApiAction action)
    {
        LOGGER.log(DEBUG, "Authkey: {0}", request.getAuthenticationKey());
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
        response.setContent(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
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
    
    private void errorResponse(ChannelHandlerContext context, ApiError error)
    {
        context.write(this.toResponse(error)).addListener(ChannelFutureListener.CLOSE);
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