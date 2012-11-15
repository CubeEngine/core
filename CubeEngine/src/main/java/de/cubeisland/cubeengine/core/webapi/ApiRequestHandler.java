package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.webapi.exception.ApiRequestException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.webapi.RequestError.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiRequestHandler extends ChannelInboundMessageHandlerAdapter<Object>
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String WEBSOCKET_ROUTE = "websocket";
    private final ApiServer server;
    private WebSocketServerHandshaker handshaker = null;
    
    ApiRequestHandler(ApiServer server)
    {
        this.server = server;
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable t)
    {
        this.error(context, UNKNOWN_ERROR);
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
            // TODO return error response: bad request
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
            return;
        }
        catch (ApiRequestException e)
        {
            // TODO add info to the error
            this.error(context, REQUEST_EXCEPTION);
        }
        catch (Throwable t)
        {
            // TODO add info to the error
            this.error(context, UNKNOWN_ERROR);
        }

        this.error(context, UNKNOWN_ERROR);
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
        context.close();
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

    public void handleEvent(String event, Map<String, Object> data)
    {

    }
}