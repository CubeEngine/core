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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.webapi.exception.ApiRequestException;
import de.cubeisland.engine.logging.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static de.cubeisland.engine.core.webapi.RequestError.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiRequestHandler extends SimpleChannelInboundHandler<Object>
{
    // TODO rewrite log messages, most of them are incomplete
    private final Charset UTF8 = Charset.forName("UTF-8");
    private final String WEBSOCKET_ROUTE = "websocket";
    private final Log log;
    private final ApiServer server;
    private WebSocketServerHandshaker handshaker = null;
    private ObjectMapper objectMapper;

    ApiRequestHandler(ApiServer server, ObjectMapper mapper)
    {
        this.server = server;
        this.objectMapper = mapper;
        this.log = server.getLog();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable t)
    {
        this.error(context, UNKNOWN_ERROR);
        this.log.error(t, "An error occurred while processing an API request!");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext context, Object message) throws Exception
    {
        this.log.info("{} connected...", ((InetSocketAddress)context.channel().remoteAddress()).getAddress()
                                                                                                  .getHostAddress());
        if (!this.server.isAddressAccepted((InetSocketAddress)context.channel().remoteAddress()))
        {
            this.log.info("Access denied!");
            context.channel().close();
        }

        if (message instanceof FullHttpRequest)
        {
            this.log.info("this is a HTTP request...");
            this.handleHttpRequest(context, (FullHttpRequest)message);
        }
        else if (message instanceof WebSocketFrame)
        {
            this.log.info("oh a websocket frame!");
            this.handleWebSocketFrame(context, (WebSocketFrame)message);
        }
        else
        {
            this.log.info("dafuq!?");
            context.close();
        }
    }

    private void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request)
    {
        if (request.getDecoderResult().isFailure())
        {
            this.error(context, UNKNOWN_ERROR);
            this.log.info(request.getDecoderResult().cause(), "The decoder failed on this request...");
            return;
        }

        QueryStringDecoder qsDecoder = new QueryStringDecoder(request.getUri(), this.UTF8, true, 100);

        String path = qsDecoder.path().trim();

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
            this.log.info("received a websocket request...");
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HOST) + "/" + this.WEBSOCKET_ROUTE, null, false);
            this.handshaker = handshakerFactory.newHandshaker(request);
            if (this.handshaker == null)
            {
                this.log.info("client is incompatible!");
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
            }
            else
            {
                this.log.debug("handshaking now...");
                this.handshaker.handshake(context.channel(), request).addListener(new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if (future.isSuccess())
                        {
                            log.debug("Success!");
                        }
                        else
                        {
                            log.debug("Failed!");
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

        JsonNode data = null;
        ByteBuf requestContent = request.content();
        if (requestContent != Unpooled.EMPTY_BUFFER)
        {
            try
            {
                data = this.objectMapper.readTree(requestContent.array());
            }
            catch (Exception ex)
            {
                this.log.debug(ex, "Failed to parse the request body!");
                this.error(context, MALFORMED_DATA);
                return;
            }
        }
        final RequestMethod method = RequestMethod.getByName(request.getMethod().name());
        final Parameters params = new Parameters(qsDecoder.parameters());

        ApiRequest apiRequest = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), method, params, request.headers(), data);
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
            this.log.debug("recevied close frame");
            this.server.unsubscribe(this);
            this.handshaker.close(context.channel(), (CloseWebSocketFrame)frame);
        }
        else if (frame instanceof PingWebSocketFrame)
        {
            this.log.debug("recevied ping frame");
            context.write(new PongWebSocketFrame(frame.content()));
        }
        else if (frame instanceof TextWebSocketFrame)
        {
            this.log.debug("recevied text frame");
            this.handleTextWebSocketFrame(context, (TextWebSocketFrame)frame);
        }
        else
        {
            this.log.info("recevied unknown incompatible frame");
            context.close();
        }
    }

    private void handleTextWebSocketFrame(ChannelHandlerContext context, TextWebSocketFrame frame)
    {
        String content = frame.text();

        int newLinePos = content.indexOf('\n');
        if (newLinePos == -1)
        {
            this.log.info("the frame data didn't contain a newline !");
            // TODO error response
            return;
        }
        String command = content.substring(0, newLinePos).trim();
        content = content.substring(newLinePos).trim();

        switch (command)
        {
            case "request":
                String route;
                newLinePos = content.indexOf('\n');
                RequestMethod method = null;
                if (newLinePos == -1)
                {
                    route = content;
                }
                else
                {
                    route = normalizeRoute(content.substring(0, newLinePos));
                    content = content.substring(newLinePos).trim();

                    final int spacePos = route.indexOf(' ');
                    if (spacePos != -1)
                    {
                        method = RequestMethod.getByName(route.substring(0, spacePos));
                        route = route.substring(spacePos + 1);
                    }
                }

                if (method == null)
                {
                    method = RequestMethod.GET;
                }

                JsonNode data = null;
                if (!content.isEmpty())
                {
                    try
                    {
                        data = this.objectMapper.readTree(content);
                    }
                    catch (Exception e)
                    {
                        // TODO ERROR
                    }
                }

                ApiHandler handler = this.server.getApiHandler(route);
                Parameters params = null;
                ApiRequest request = new ApiRequest((InetSocketAddress)context.channel()
                                                                              .remoteAddress(), method, params, HttpHeaders.EMPTY_HEADERS, data);
                ApiResponse response = new ApiResponse();
                try
                {
                    handler.execute(request, response);
                }
                catch (Exception ignored)
                {
                }
                break;
            case "subscribe":
                this.server.subscribe(content.trim(), this);
                break;
            case "unsubscribe":
                this.server.unsubscribe(content.trim(), this);
                break;
        }

        context.write(new TextWebSocketFrame(command + " -- " + content));
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
        Map<String, Object> data = new HashMap<>();
        data.put("id", error.getCode());
        data.put("desc", error.getDescription());

        if (e != null)
        {
            Map<String, Object> reason = new HashMap<>();
            reason.put("id", e.getCode());
            reason.put("desc", e.getMessage());
            data.put("reason", reason);
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, error.getRepsonseStatus(), Unpooled.copiedBuffer(this.serialize(data), this.UTF8));
        response.headers().set(CONTENT_TYPE, MimeType.JSON.toString());
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE).addListener(
            ChannelFutureListener.CLOSE_ON_FAILURE);
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
                this.log.error(e, "Failed to generate the JSON code for a response!");
                return "null";
            }
        }
        else
        {
            return String.valueOf(object);
        }
    }

    public void handleEvent(String event, Map<String, Object> data)
    {}
}
