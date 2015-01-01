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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class IpFilter extends ChannelInboundHandlerAdapter
{
    private final ApiServer server;

    public IpFilter(ApiServer server)
    {
        this.server = server;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        if (ctx.channel().isActive() && !accepts((InetSocketAddress)ctx.channel().remoteAddress()))
        {
            reject(ctx);
            return;
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if (!accepts((InetSocketAddress)ctx.channel().remoteAddress()))
        {
            reject(ctx);
            return;
        }
        super.channelActive(ctx);
    }

    private boolean accepts(InetSocketAddress address)
    {
        if (server.isBlacklistEnabled())
        {
            if (server.isBlacklisted(address))
            {
                return false;
            }
        }
        if (server.isWhitelistEnabled())
        {
            return server.isWhitelisted(address);
        }
        return true;
    }

    private void reject(ChannelHandlerContext ctx)
    {
        ctx.close();
    }
}
