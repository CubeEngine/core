package de.cubeisland.cubeengine.core.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.webapi.exception.ApiRequestException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.util.log.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.webapi.RequestError.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiRequestHandler extends ChannelInboundMessageHandlerAdapter<Object>
{
    private static final Logger LOGGER = new CubeLogger("webapi");

    static
    {
        try
        {
            LOGGER.addHandler(new FileHandler("webapi.log"));
        }
        catch (IOException e)
        {
            CubeEngine.getLogger().log(ERROR, "Failed to initialize the file handler for the web api log!", e);
        }
    }
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String WEBSOCKET_ROUTE = "websocket";
    private final ApiServer server;
    private WebSocketServerHandshaker handshaker = null;
    private ObjectMapper objectMapper;
    
    ApiRequestHandler(ApiServer server)
    {
        this.server = server;
        this.objectMapper = CubeEngine.getJsonObjectMapper();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable t)
    {
        this.error(context, UNKNOWN_ERROR);
        LOGGER.log(ERROR, "An error occurred while processing an API request: " + t.getMessage(), t);
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext context, Object message) throws Exception
    {
        LOGGER.log(INFO, "{0} connected...", ((InetSocketAddress)context.channel().remoteAddress()).getAddress().getHostAddress());
        if (!this.server.isAddressAccepted((InetSocketAddress)context.channel().remoteAddress()))
        {
            LOGGER.log(INFO, "Access denied!");
            context.channel().close();
        }

        if (message instanceof HttpRequest)
        {
            LOGGER.log(INFO, "this is a HTTP request...");
            this.handleHttpRequest(context, (HttpRequest)message);
        }
        else if (message instanceof WebSocketFrame)
        {
            LOGGER.log(INFO, "oh a websocket frame!");
            this.handleWebSocketFrame(context, (WebSocketFrame)message);
        }
        else
        {
            LOGGER.log(INFO, "dafuq!?");
            context.close();
        }
    }
    
    private void handleHttpRequest(ChannelHandlerContext context, HttpRequest request)
    {
        if (request.getDecoderResult().isFailure())
        {
            this.error(context, UNKNOWN_ERROR);
            LOGGER.log(INFO, "the decoder failed on this request...", request.getDecoderResult().cause());
            return;
        }
        
        QueryStringDecoder qsDecoder = new QueryStringDecoder(request.getUri(), UTF8, true, 100);
        
        String path = qsDecoder.getPath().trim();
        final Map<String, List<String>> getParams = qsDecoder.getParameters();
        
        if (path.length() == 0 || "/".equals(path))
        {
            this.error(context, ROUTE_NOT_FOUND);
            return;
        }
        
        if (path.charAt(0) == '/')
        {
            path = path.substring(1);
        }
        if (path.charAt(path.length() - 1) == '/')
        {
            path = path.substring(0, path.length() - 1);
        }
        
        // is this request intended to initialize a websockets connection?
        if ("websocket".equals(path))
        {
            LOGGER.log(INFO, "received a websocket request...");
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory("ws://" + request.getHeader(HOST) + "/" + WEBSOCKET_ROUTE, null, false);
            this.handshaker = handshakerFactory.newHandshaker(request);
            if (this.handshaker == null)
            {
                LOGGER.log(INFO, "client is incompatible!");
                handshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
            }
            else
            {
                LOGGER.log(INFO, "handshaking now...");
                this.handshaker.handshake(context.channel(), request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if (future.isSuccess())
                        {
                            LOGGER.log(INFO, "Success!");
                        }
                        else
                        {
                            LOGGER.log(INFO, "Failed!");
                        }
                    }
                });
            }
            return;
        }

        ApiHandler handler = this.server.getApiHandler(path);
        if (handler == null)
        {
            this.error(context, ROUTE_NOT_FOUND);
            return;
        }

        ApiRequest apiRequest = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), RequestMethod.getByName(request.getMethod().getName()), null, request.getHeaders());
        ApiResponse apiResponse = new ApiResponse();

        try
        {
            handler.execute(apiRequest, apiResponse);
            this.success(context, apiResponse);
        }
        catch (ApiRequestException e)
        {
            this.error(context, REQUEST_EXCEPTION, e);
        }
        catch (Throwable t)
        {
            this.error(context, UNKNOWN_ERROR);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext context, WebSocketFrame frame)
    {
        if (frame instanceof CloseWebSocketFrame)
        {
            LOGGER.log(INFO, "recevied close frame");
            // TODO remove subscriptions
            this.handshaker.close(context.channel(), (CloseWebSocketFrame)frame);
        }
        else if (frame instanceof PingWebSocketFrame)
        {
            LOGGER.log(INFO, "recevied ping frame");
            context.write(new PongWebSocketFrame(frame.getBinaryData()));
        }
        else if (frame instanceof TextWebSocketFrame)
        {
            LOGGER.log(INFO, "recevied text frame");
            this.handleTextWebSocketFrame(context, (TextWebSocketFrame)frame);
        }
        else
        {
            LOGGER.log(INFO, "recevied unknown incompatible frame");
            context.close();
        }
    }

    private void handleTextWebSocketFrame(ChannelHandlerContext context, TextWebSocketFrame frame)
    {
        String text = frame.getText();
        
        int newLinePos = text.indexOf('\n');
        if (newLinePos == -1)
        {
            LOGGER.log(INFO, "the frame data didn't contain a newline !");
            // TODO error response
            return;
        }
        String command = text.substring(0, newLinePos).trim();
        text = text.substring(newLinePos).trim();

        if ("request".equals(command))
        {
            String route;
            newLinePos = text.indexOf('\n');
            if (newLinePos == -1)
            {
                route = text;
            }
            else
            {
                route = normalizeRoute(text.substring(0, newLinePos));
                text = text.substring(newLinePos).trim();
            }

            ApiHandler handler = this.server.getApiHandler(route);
            ApiRequest request = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), null, null, null);
            ApiResponse response = new ApiResponse();
            try
            {
                handler.execute(request, response);
            }
            catch (ApiRequestException e)
            {

            }
            catch (Throwable t)
            {

            }
        }
        else if ("subscribe".equals(command))
        {
            this.server.subscribe(text.trim(), this);
        }
        else if ("unsubscribe".equals(command))
        {
            this.server.unsubscribe(text.trim(), this);
        }
        
        context.write(new TextWebSocketFrame(command + " -- " + text));
    }

    private void success(ChannelHandlerContext context, ApiResponse apiResponse)
    {
        context.write(apiResponse.getContent()).addListener(ChannelFutureListener.CLOSE);
    }

    private void error(ChannelHandlerContext context, RequestError error)
    {
        this.error(context, error, null);
    }

    private void error(ChannelHandlerContext context, RequestError error, ApiRequestException e)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", error.getCode());
        data.put("desc", error.getDescription());

        if (e != null)
        {
            Map<String, Object> reason = new HashMap<String, Object>();
            reason.put("id", e.getCode());
            reason.put("desc", e.getMessage());
            data.put("reason", reason);
        }

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, error.getRepsonseStatus());
        response.setContent(Unpooled.copiedBuffer(this.serialize(data), UTF8));
        response.setHeader(CONTENT_TYPE, MimeType.JSON.toString());

        context.write(response).addListener(ChannelFutureListener.CLOSE).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    public static String normalizeRoute(String route)
    {
        route = route.trim().replace('\\', '/');
        if (route.charAt(0) == '/')
        {
            route = route.substring(1);
        }
        if (route.charAt(route.length() - 1) == '/')
        {
            route = route.substring(0, route.length() - 1);
        }
        return route;
    }

    public String serialize(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        if (object instanceof Map)
        {
            try
            {
                return this.objectMapper.writer().writeValueAsString(object);
            }
            catch (JsonProcessingException e)
            {
                LOGGER.log(ERROR, "Failed to generate the JSON code for a response!", e);
                return "null";
            }
        }
        else
        {
            return String.valueOf(object);
        }
    }

    public void handleEvent(String event, Map<String, Object> data)
    {

    }
}