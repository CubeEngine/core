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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.logging.Log;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static de.cubeisland.engine.core.webapi.HttpRequestHandler.normalizePath;
import static de.cubeisland.engine.core.webapi.RequestMethod.GET;
import static de.cubeisland.engine.core.webapi.RequestMethod.getByName;
import static io.netty.handler.codec.http.HttpHeaders.EMPTY_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

public class WebSocketRequestHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{
    private final String WEBSOCKET_ROUTE = "websocket";
    private final Charset UTF8 = Charset.forName("UTF-8");
    private final Log log;
    private final Core core;
    private final ApiServer server;
    private WebSocketServerHandshaker handshaker = null;
    private ObjectMapper objectMapper;
    private User authUser;

    private ChannelHandlerContext last;

    public WebSocketRequestHandler(Core core, ApiServer server, ObjectMapper mapper, User authUser)
    {
        this.core = core;
        this.server = server;
        this.objectMapper = mapper;
        this.authUser = authUser;
        this.log = server.getLog();
    }

    public void doHandshake(ChannelHandlerContext ctx, FullHttpRequest message)
    {
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory("ws://" + message.headers().get(HOST) + "/" + this.WEBSOCKET_ROUTE, null, false);
        this.handshaker = handshakerFactory.newHandshaker(message);
        if (handshaker == null)
        {
            this.log.info("client is incompatible!");
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            return;
        }
        this.log.debug("handshaking now...");
        this.handshaker.handshake(ctx.channel(), message).addListener(new ChannelFutureListener()
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

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception
    {
        this.last = ctx;
        if (frame instanceof CloseWebSocketFrame)
        {
            this.log.debug("recevied close frame");
            this.server.unsubscribe(this);
            this.handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame);
        }
        else if (frame instanceof PingWebSocketFrame)
        {
            this.log.debug("recevied ping frame");
            ctx.write(new PongWebSocketFrame(frame.content()));
        }
        else if (frame instanceof TextWebSocketFrame)
        {
            this.log.debug("recevied text frame");
            this.handleTextWebSocketFrame(ctx, (TextWebSocketFrame)frame);
        }
        else
        {
            this.log.info("recevied unknown incompatible frame");
            ctx.close();
        }
    }

    private void handleTextWebSocketFrame(final ChannelHandlerContext ctx, TextWebSocketFrame frame)
    {
        // TODO log exceptions!!!
        JsonNode jsonNode;
        try
        {
            jsonNode = objectMapper.readTree(frame.text());
        }
        catch (IOException e)
        {
            this.log.info("the frame data was no valid json!");
            return;
        }
        JsonNode action = jsonNode.get("action");
        JsonNode msgid = jsonNode.get("msgid");
        ObjectNode responseNode = objectMapper.createObjectNode();
        if (action == null)
        {
            responseNode.put("response", "No action");
        }
        else
        {
            JsonNode data = jsonNode.get("data");
            switch (action.asText())
            {
                case "http":
                    QueryStringDecoder qsDecoder = new QueryStringDecoder(normalizePath(data.get("uri").asText()), this.UTF8, true, 100);

                    JsonNode reqMethod = data.get("method");
                    RequestMethod method = reqMethod != null ? getByName(reqMethod.asText()) : GET;
                    JsonNode reqdata = data.get("body");
                    ApiHandler handler = this.server.getApiHandler(normalizePath(qsDecoder.path()));
                    if (handler == null)
                    {
                        responseNode.put("response", "Unknown route");
                        break;
                    }
                    Parameters params = new Parameters(qsDecoder.parameters(),
                                                       core.getCommandManager().getReaderManager());
                    ApiRequest request = new ApiRequest((InetSocketAddress)ctx.channel().remoteAddress(), method, params, EMPTY_HEADERS, reqdata, authUser);
                    ApiResponse response = handler.execute(request);
                    if (msgid != null)
                    {
                        responseNode.put("response", objectMapper.valueToTree(response.getContent()));
                    }
                    break;
                case "subscribe":
                    this.server.subscribe(data.asText().trim(), this);
                    break;
                case "unsubscribe":
                    this.server.unsubscribe(data.asText().trim(), this);
                    break;
                default:
                    responseNode.put("response", action.asText() + " -- " + data.asText());
            }

        }
        if (msgid != null && responseNode.elements().hasNext())
        {
            responseNode.put("msgid", msgid);
            ctx.writeAndFlush(responseNode);
        }
    }

    public void handleEvent(String event, ObjectNode data)
    {
        data.put("event", event);
        this.last.writeAndFlush(data);
    }
}
