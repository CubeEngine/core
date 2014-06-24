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
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.webapi.sender.ApiCommandSender;
import de.cubeisland.engine.core.webapi.sender.ApiServerSender;
import de.cubeisland.engine.core.webapi.sender.ApiUser;
import de.cubeisland.engine.logging.Log;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static de.cubeisland.engine.core.webapi.HttpRequestHandler.normalizeRoute;
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
    private ApiCommandSender cmdSender;

    public WebSocketRequestHandler(Core core, ApiServer server, ObjectMapper mapper, User authUser)
    {
        this.core = core;
        this.server = server;
        this.objectMapper = mapper;
        this.cmdSender = authUser == null ? new ApiServerSender(core, mapper) : new ApiUser(core, authUser, mapper);
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
        try
        {
            JsonNode jsonNode = objectMapper.readTree(frame.text());
            JsonNode command = jsonNode.get("command");
            if (command == null)
            {
                ctx.writeAndFlush(new TextWebSocketFrame("No command!"));
                return;
            }
            final JsonNode data = jsonNode.get("data");
            switch (command.asText())
            {
                case "request":
                    String route = normalizeRoute(jsonNode.get("route").asText());
                    JsonNode reqMethod = data.get("requestmethod");
                    RequestMethod method = reqMethod != null ? RequestMethod.getByName(reqMethod.asText()) : RequestMethod.GET;
                    JsonNode reqdata = data.get("data");

                    ApiHandler handler = this.server.getApiHandler(route);
                    Parameters params = null;
                    ApiRequest request = new ApiRequest((InetSocketAddress)ctx.channel().remoteAddress(), method, params, HttpHeaders.EMPTY_HEADERS, reqdata);
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
                    this.server.subscribe(data.asText().trim(), this);
                    break;
                case "unsubscribe":
                    this.server.unsubscribe(data.asText().trim(), this);
                    break;
                case "command":
                    core.getTaskManager().callSync(new Callable<Object>()
                    {
                        @Override
                        public Object call() throws Exception
                        {
                            core.getCommandManager().runCommand(cmdSender, data.asText());
                            cmdSender.flush(ctx);
                            return null;
                        }
                    });

                    break;
                default:
                    ctx.writeAndFlush(command + " -- " + data.asText());
            }
        }
        catch (IOException e)
        {
            this.log.info("the frame data was no valid json!");
            ctx.writeAndFlush(new TextWebSocketFrame("Invalid json!"));
        }
    }

    public void handleEvent(String event, Map<String, Object> data)
    {}
}
