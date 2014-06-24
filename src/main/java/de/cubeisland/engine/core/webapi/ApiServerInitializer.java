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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class ApiServerInitializer extends ChannelInitializer<SocketChannel>
{
    private final Core core;
    private final ApiServer server;
    private final ObjectMapper objectMapper;

    ApiServerInitializer(Core core, ApiServer server)
    {
        this.core = core;
        this.server = server;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ch.pipeline()
            .addLast("ipfilter", new IpFilter(server))
            .addLast("iplimiter", new IpLimiter(server.getMaxConnectionCount()))
            .addLast("decoder", new HttpRequestDecoder())
            .addLast("aggregator", new HttpObjectAggregator(this.server.getMaxContentLength()))
            .addLast("encoder", new HttpResponseEncoder())
            .addLast("httpHandler", new HttpRequestHandler(core, this.server, this.objectMapper));
        if (this.server.isCompressionEnabled())
        {
            ch.pipeline().addLast("deflater", new HttpContentCompressor(this.server.getCompressionLevel(), this.server.getCompressionWindowBits(), this.server.getCompressionMemoryLevel()));
        }
    }
}
