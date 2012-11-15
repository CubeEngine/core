package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.CubeEngine;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.INFO;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

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
        LOGGER.log(INFO, "error inc!");
        context.close();
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext context, Object message) throws Exception
    {
        LOGGER.log(INFO, "{0} connected...", ((InetSocketAddress)context.channel().remoteAddress()).getAddress().getHostAddress());
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
            LOGGER.log(INFO, "the decoder failed on this request...", request.getDecoderResult().cause());
            // TODO return error response: bad request
            return;
        }
        
        QueryStringDecoder qsDecoder = new QueryStringDecoder(request.getUri(), UTF8, true, 100);
        
        String path = qsDecoder.getPath().trim();
        final Map<String, List<String>> getParams = qsDecoder.getParameters();
        
        if (path.length() == 0 || "/".equals(path))
        {
            LOGGER.log(INFO, "no proper path...");
            context.close();
            // TODO return error response
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
        
        HttpResponse response = new DefaultHttpResponse(HTTP_1_0, OK);
        response.setContent(Unpooled.copiedBuffer(path, UTF8));
        
        context.write(response).addListener(ChannelFutureListener.CLOSE);
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
            context.close().addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleTextWebSocketFrame(ChannelHandlerContext context, TextWebSocketFrame frame)
    {
        String text = frame.getText();
        
        final int newLinePos = text.indexOf('\n');
        if (newLinePos == -1)
        {
            LOGGER.log(INFO, "the frame data didn't contain a newline !");
            // TODO error response
            return;
        }
        String command = text.substring(0, newLinePos).trim();
        text = text.substring(newLinePos).trim();
        
        context.write(new TextWebSocketFrame(command + " -- " + text));
    }
}