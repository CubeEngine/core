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
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.service.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.webapi.exception.ApiRequestException;
import de.cubeisland.engine.logging.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

import static de.cubeisland.engine.core.webapi.MimeType.JSON;
import static de.cubeisland.engine.core.webapi.RequestStatus.*;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
    // TODO rewrite log messages, most of them are incomplete
    private final Charset UTF8 = Charset.forName("UTF-8");
    private final String WEBSOCKET_ROUTE = "websocket";
    private final Log log;
    private final Core core;
    private final ApiServer server;
    private ObjectMapper objectMapper;

    HttpRequestHandler(Core core, ApiServer server, ObjectMapper mapper)
    {
        this.core = core;
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
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest message) throws Exception
    {
        InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        this.log.info("{} connected...", inetSocketAddress.getAddress().getHostAddress());
        if (!this.server.isAddressAccepted(inetSocketAddress.getAddress()))
        {
            this.log.info("Access denied!");
            ctx.channel().close();
        }

        if (message.getDecoderResult().isFailure())
        {
            this.error(ctx, UNKNOWN_ERROR);
            this.log.info(message.getDecoderResult().cause(), "The decoder failed on this request...");
            return;
        }

        boolean authorized = this.server.isAuthorized(inetSocketAddress.getAddress());
        QueryStringDecoder qsDecoder = new QueryStringDecoder(message.getUri(), this.UTF8, true, 100);
        final Parameters params = new Parameters(qsDecoder.parameters());
        User authUser = null;
        if (!authorized)
        {
            if (!core.getModuleManager().getServiceManager().isImplemented(Permission.class))
            {
                this.error(ctx, AUTHENTICATION_FAILURE, new ApiRequestException("Authentication deactivated", 200));
                return;
            }
            String user = params.get("user", String.class);
            String pass = params.get("pass", String.class);
            if (user == null || pass == null)
            {
                this.error(ctx, AUTHENTICATION_FAILURE, new ApiRequestException("Could not complete authentication", 200));
                return;
            }
            User exactUser = core.getUserManager().findExactUser(user);
            if (exactUser == null || !exactUser.isPasswordSet() || !CubeEngine.getUserManager().checkPassword(exactUser, pass))
            {
                this.error(ctx, AUTHENTICATION_FAILURE, new ApiRequestException("Could not complete authentication", 200));
                return;
            }
            authUser = exactUser;
        }
        String path = qsDecoder.path().trim();
        if (path.length() == 0 || "/".equals(path))
        {
            this.error(ctx, ROUTE_NOT_FOUND);
            return;
        }
        path = normalizePath(path);

        // is this request intended to initialize a websockets connection?
        if (WEBSOCKET_ROUTE.equals(path))
        {
            WebSocketRequestHandler handler = null;
            if (!(ctx.pipeline().last() instanceof WebSocketRequestHandler))
            {
                handler = new WebSocketRequestHandler(core, server, objectMapper, authUser);
                ctx.pipeline().addLast("wsEncoder", new TextWebSocketFrameEncoder(objectMapper));
                ctx.pipeline().addLast("handler", handler);
            }
            else
            {
                handler = (WebSocketRequestHandler)ctx.pipeline().last();
            }
            this.log.info("received a websocket request...");
            handler.doHandshake(ctx, message);
            return;
        }

        this.handleHttpRequest(ctx, message, path, params, authUser);
    }

    private void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest message, String path, Parameters params, User authUser)
    {
        ApiHandler handler = this.server.getApiHandler(path);
        if (handler == null)
        {
            this.error(context, ROUTE_NOT_FOUND);
            return;
        }

        JsonNode data = null;
        ByteBuf requestContent = message.content();
        if (!requestContent.equals(EMPTY_BUFFER))
        {
            try
            {
                byte[] bytes = new byte[requestContent.readableBytes()];
                requestContent.readBytes(bytes);
                data = this.objectMapper.readTree(bytes);
            }
            catch (Exception ex)
            {
                this.log.debug(ex, "Failed to parse the request body!");
                this.error(context, MALFORMED_DATA);
                return;
            }
        }
        final RequestMethod method = RequestMethod.getByName(message.getMethod().name());

        ApiRequest apiRequest = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), method, params, message.headers(), data,
                                               authUser);
        try
        {
            this.success(context, handler.execute(apiRequest));
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

    private void success(ChannelHandlerContext context, ApiResponse apiResponse)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(this.serialize(apiResponse.getContent()), this.UTF8));
        response.headers().set(CONTENT_TYPE, JSON.toString());
        context.writeAndFlush(response).addListener(CLOSE);
    }

    private void error(ChannelHandlerContext context, RequestStatus error)
    {
        this.error(context, error, null);
    }

    private void error(ChannelHandlerContext context, RequestStatus error, ApiRequestException e)
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

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, error.getRepsonseStatus(), Unpooled.copiedBuffer(this.serialize(data), this.UTF8));
        response.headers().set(CONTENT_TYPE, JSON.toString());
        context.writeAndFlush(response).addListener(CLOSE).addListener(CLOSE_ON_FAILURE);
    }

    public static String normalizePath(String route)
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
}
