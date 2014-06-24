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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class IpLimiter extends ChannelHandlerAdapter
{
    private final int maxConnectionCount;
    private Map<InetAddress, Integer> connections = new HashMap<>();

    public IpLimiter(int maxConnectionCount)
    {
        this.maxConnectionCount = maxConnectionCount;
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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        InetAddress address = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress();
        Integer conCount = connections.remove(address);
        if (conCount != null && conCount != 1)
        {
            connections.put(address, conCount - 1);
        }
        super.channelInactive(ctx);
    }

    private boolean accepts(InetSocketAddress address)
    {
        if (connections.containsKey(address.getAddress()))
        {
            Integer connectionCount = connections.get(address.getAddress());
            if (maxConnectionCount <= connectionCount)
            {
                return false;
            }
            connections.put(address.getAddress(), connectionCount + 1);
        }
        else
        {
            connections.put(address.getAddress(), 1);
        }
        return true;
    }

    private void reject(ChannelHandlerContext ctx)
    {
        ctx.close();
    }
}
