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

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.logging.LoggingUtil;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.webapi.exception.ApiStartupException;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang.Validate;

/**
 * This class represents the API server and provides methods to configure and
 * controll it
 */
public class ApiServer
{
    private final Core core;
    private final Log log;
    private final AtomicInteger maxContentLength;
    private final AtomicBoolean compress;
    private final AtomicInteger compressionLevel;
    private final AtomicInteger windowBits;
    private final AtomicInteger memoryLevel;
    private final AtomicReference<InetAddress> bindAddress;
    private final AtomicInteger port;
    private final AtomicReference<ServerBootstrap> bootstrap;
    private final AtomicReference<EventLoopGroup> eventLoopGroup;
    private final AtomicReference<Channel> channel;
    private final AtomicInteger maxThreads;
    private final Set<String> disabledRoutes;
    private final AtomicBoolean enableWhitelist;
    private final Set<String> whitelist;
    private final AtomicBoolean enableBlacklist;
    private final Set<String> blacklist;
    private final ConcurrentMap<String, ApiHandler> handlers;
    private final ConcurrentMap<String, List<ApiRequestHandler>> subscriptions;

    public ApiServer(Core core)
    {
        this.core = core;
        this.log = core.getLogFactory().getLog(Core.class, "WebAPI");
        this.log.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "WebAPI"),
                                                  LoggingUtil.getFileFormat(true, true),
                                                  true, LoggingUtil.getCycler(),
                                                  core.getTaskManager().getThreadFactory()));

        this.bootstrap = new AtomicReference<>(null);
        this.eventLoopGroup = new AtomicReference<>(null);
        this.channel = new AtomicReference<>(null);
        this.bindAddress = new AtomicReference<>(null);
        this.maxThreads = new AtomicInteger(2);
        try
        {
            this.bindAddress.set(InetAddress.getLocalHost());
        }
        catch (UnknownHostException ignored)
        {
            this.log.warn("Failed to get the localhost!");
        }
        this.port = new AtomicInteger(6561);
        this.maxContentLength = new AtomicInteger(1048576);

        this.compress = new AtomicBoolean(false);
        this.compressionLevel = new AtomicInteger(9);
        this.windowBits = new AtomicInteger(15);
        this.memoryLevel = new AtomicInteger(9);

        this.disabledRoutes = new CopyOnWriteArraySet<>();
        this.enableWhitelist = new AtomicBoolean(false);
        this.whitelist = new CopyOnWriteArraySet<>();
        this.enableBlacklist = new AtomicBoolean(false);
        this.blacklist = new CopyOnWriteArraySet<>();

        this.handlers = new ConcurrentHashMap<>();
        this.subscriptions = new ConcurrentHashMap<>();
    }

    public Log getLog()
    {
        return this.log;
    }


    public void configure(final ApiConfig config)
    {
        assert config != null: "The config must not be null!";

        try
        {
            this.setBindAddress(config.network.address);
        }
        catch (UnknownHostException ignored)
        {
            this.log.warn("Failed to resolve the host {}, ignoring the value...");
        }
        this.setPort(config.network.port);
        this.setMaxThreads(config.network.maxThreads);

        this.setCompressionEnabled(config.compression.enable);
        this.setCompressionLevel(config.compression.level);
        this.setCompressionWindowBits(config.compression.windowBits);
        this.setCompressionMemoryLevel(config.compression.memoryLevel);

        this.setWhitelistEnabled(config.whitelist.enable);
        this.setWhitelist(config.whitelist.ips);

        this.setBlacklistEnabled(config.blacklist.enable);
        this.setBlacklist(config.blacklist.ips);
    }

    /**
     * Starts the server
     *
     * @return fluent interface
     */
    public ApiServer start() throws ApiStartupException
    {
        if (!this.isRunning())
        {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();

            try
            {
                this.eventLoopGroup.set(new NioEventLoopGroup(this.maxThreads.get(), this.core.getTaskManager()
                                                                                              .getThreadFactory()));
                serverBootstrap.group(this.eventLoopGroup.get())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ApiServerInitializer(this.core, this))
                    .localAddress(this.bindAddress.get(), this.port.get());

                this.bootstrap.set(serverBootstrap);
                this.channel.set(serverBootstrap.bind().sync().channel());
            }
            catch (Exception e)
            {
                this.bootstrap.set(null);
                this.channel.set(null);
                this.eventLoopGroup.getAndSet(null).shutdownGracefully(2, 5, TimeUnit.SECONDS);
                throw new ApiStartupException("The API server failed to start!", e);
            }
        }
        return this;
    }

    /**
     * Stops the server
     *
     * @return fluent interface
     */
    public ApiServer stop()
    {
        if (this.isRunning())
        {
            this.bootstrap.set(null);
            this.channel.set(null);
            this.eventLoopGroup.getAndSet(null).shutdownGracefully(2, 5, TimeUnit.SECONDS);
        }
        return this;
    }

    /**
     * Returns whether the server is running or not
     *
     * @return true if it is running
     */
    public boolean isRunning()
    {
        return (this.channel.get() != null && this.channel.get().isOpen());
    }

    public ApiHandler getApiHandler(String route)
    {
        if (route == null)
        {
            return null;
        }
        return this.handlers.get(route);
    }

    public void registerApiHandlers(final ApiHolder holder)
    {
        assert holder != null: "The API holder must not be null!";

        String route;
        Action actionAnnotation;
        for (Method method : holder.getClass().getDeclaredMethods())
        {
            actionAnnotation = method.getAnnotation(Action.class);
            if (actionAnnotation != null)
            {
                route = ApiRequestHandler.normalizeRoute(actionAnnotation.route());
                this.handlers.put(route, new ApiHandler(holder, route, method, actionAnnotation.auth(), actionAnnotation.parameters(), actionAnnotation.methods()));
            }
        }
    }

    public void unregisterApiHandler(String route)
    {
        this.handlers.remove(route);
    }

    public void unregisterApiHandlers(Module module)
    {
        Iterator<Map.Entry<String, ApiHandler>> iter = this.handlers.entrySet().iterator();

        ApiHandler handler;
        while (iter.hasNext())
        {
            handler = iter.next().getValue();
            if (handler.getModule() == module)
            {
                iter.remove();
            }
        }
    }

    public void unregisterApiHandlers(ApiHolder holder)
    {
        Iterator<Map.Entry<String, ApiHandler>> iter = this.handlers.entrySet().iterator();

        ApiHandler handler;
        while (iter.hasNext())
        {
            handler = iter.next().getValue();
            if (handler.getHolder() == holder)
            {
                iter.remove();
            }
        }
    }

    public void unregisterApiHandlers()
    {
        this.handlers.clear();
    }

    public void setBindAddress(String address) throws UnknownHostException
    {
        this.setBindAddress(InetAddress.getByName(address));
    }

    /**
     * Sets the address the server will bind to on the next start
     *
     * @param address the address
     */
    public void setBindAddress(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        this.bindAddress.set(address);
    }

    /**
     * Returns the address the server is bound/will bind to
     *
     * @return the address
     */
    public InetAddress getBindAddress()
    {
        return this.bindAddress.get();
    }

    public InetAddress getBoundAddress()
    {
        if (this.isRunning())
        {
            return ((InetSocketAddress)this.channel.get().localAddress()).getAddress();
        }
        return null;
    }

    public void setPort(short port)
    {
        this.port.set(port);
    }

    /**
     * Returns the port the server is/will be listening on
     *
     * @return the post
     */
    public int getPort()
    {
        return this.port.get();
    }

    public short getBoundPort()
    {
        if (this.isRunning())
        {
            return (short)((InetSocketAddress)this.channel.get().localAddress()).getPort();
        }
        return -1;
    }

    public void setMaxThreads(int maxThreads)
    {
        this.maxThreads.set(maxThreads);
    }

    public int getMaxThreads()
    {
        return this.maxThreads.get();
    }

    public void setMaxContentLength(int mcl)
    {
        this.maxContentLength.set(mcl);
    }

    /**
     * Returns the maximum content length the client may send
     *
     * @return the maximum content length
     */
    public int getMaxContentLength()
    {
        return this.maxContentLength.get();
    }

    public void setCompressionEnabled(boolean state)
    {
        this.compress.set(state);
    }

    public boolean isCompressionEnabled()
    {
        return this.compress.get();
    }

    public void setCompressionLevel(int level)
    {
        this.compressionLevel.set(Math.max(1, Math.min(9, level)));
    }

    public int getCompressionLevel()
    {
        return this.compressionLevel.get();
    }

    public void setCompressionWindowBits(int bits)
    {
        this.windowBits.set(Math.max(9, Math.min(15, bits)));
    }

    public int getCompressionMemoryLevel()
    {
        return this.memoryLevel.get();
    }

    public void setCompressionMemoryLevel(int level)
    {
        this.memoryLevel.set(Math.max(1, Math.min(9, level)));
    }

    public int getCompressionWindowBits()
    {
        return this.windowBits.get();
    }

    /**
     * Returns whether whitelisting is enabled
     *
     * @return true if enabled
     */
    public boolean isWhitelistEnabled()
    {
        return this.enableWhitelist.get();
    }

    /**
     * Sets the enabled state of the whitelisting
     *
     * @param state true to enable, false to disable
     */
    public void setWhitelistEnabled(boolean state)
    {
        this.enableWhitelist.set(state);
    }

    public void whitelistAddress(String address)
    {
        this.whitelist.add(address);
    }

    public void whitelistAddress(InetAddress address)
    {
        this.whitelistAddress(address.getHostAddress());
    }

    public void whitelistAddress(InetSocketAddress address)
    {
        this.whitelistAddress(address.getAddress());
    }

    public void unWhitelistAddress(String address)
    {
        this.whitelist.remove(address);
    }

    public void unWhitelistAddress(InetAddress address)
    {
        this.unWhitelistAddress(address.getHostAddress());
    }

    public void unWhitelistAddress(InetSocketAddress address)
    {
        this.unWhitelistAddress(address.getAddress());
    }

    public void setWhitelist(Set<String> newWhitelist)
    {
        assert newWhitelist != null: "The whitelist must not be null!";
        Validate.noNullElements(newWhitelist, "The whitelist must not contain null values!");

        this.whitelist.clear();
        this.whitelist.addAll(newWhitelist);
    }

    /**
     * Checks whether an string representation of an IP is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(String ip)
    {
        return !this.enableWhitelist.get() || this.whitelist.contains(ip);
    }

    /**
     * Checks whether an InetAddress is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(InetAddress ip)
    {
        return this.isWhitelisted(ip.getHostAddress());
    }

    /**
     * Checks whether an InetSocketAddress is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(InetSocketAddress ip)
    {
        return this.isWhitelisted(ip.getAddress());
    }

    /**
     * Sets the enabled state of the blacklisting
     *
     * @param state true to enable, false to disable
     */
    public void setBlacklistEnabled(boolean state)
    {
        this.enableBlacklist.set(state);
    }

    public void blacklistAddress(String address)
    {
        this.blacklist.add(address);
    }

    public void blacklistAddress(InetAddress address)
    {
        this.blacklistAddress(address.getHostAddress());
    }

    public void blacklistAddress(InetSocketAddress address)
    {
        this.blacklistAddress(address.getAddress());
    }

    public void unBlacklistAddress(String address)
    {
        this.blacklist.remove(address);
    }

    public void unBlacklistAddress(InetAddress address)
    {
        this.unBlacklistAddress(address.getHostAddress());
    }

    public void unBlacklistAddress(InetSocketAddress address)
    {
        this.unBlacklistAddress(address.getAddress());
    }

    public void setBlacklist(Set<String> newBlacklist)
    {
        assert newBlacklist != null: "The blacklist must not be null!";
        Validate.noNullElements(newBlacklist, "The blacklist must not contain null values!");

        this.blacklist.clear();
        this.blacklist.addAll(newBlacklist);
    }

    /**
     * Returns whether blacklisting is enabled
     *
     * @return true if it is
     */
    public boolean isBlacklistEnabled()
    {
        return this.enableBlacklist.get();
    }

    /**
     * Checks whether an InetSocketAddress is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(InetSocketAddress ip)
    {
        return this.isBlacklisted(ip.getAddress());
    }

    /**
     * Checks whether an InetAddress is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(InetAddress ip)
    {
        return this.isBlacklisted(ip.getHostAddress());
    }

    /**
     * Checks whether a string representation of an IP is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(String ip)
    {
        return this.enableBlacklist.get() && this.blacklist.contains(ip);
    }

    public boolean isRouteDisabled(String route)
    {
        return this.disabledRoutes.contains(route);
    }

    public void disableRoute(String route)
    {
        this.disabledRoutes.add(route);
    }

    public void reenableRoute(String route)
    {
        this.disabledRoutes.remove(route);
    }

    public void reenableRoutes(String... routes)
    {
        for (String route : routes)
        {
            this.reenableRoute(route);
        }
    }

    public boolean isAddressAccepted(String address)
    {
        return this.isWhitelisted(address) && !this.isBlacklisted(address);
    }

    public boolean isAddressAccepted(InetAddress address)
    {
        return this.isAddressAccepted(address.getHostAddress());
    }

    public boolean isAddressAccepted(InetSocketAddress address)
    {
        return this.isAddressAccepted(address.getAddress());
    }

    public void subscribe(String event, ApiRequestHandler requestHandler)
    {
        assert event != null: "The event name must not be null!";
        assert requestHandler != null: "The request handler must not be null!";
        event = event.toLowerCase(Locale.ENGLISH);

        List<ApiRequestHandler> subscribedHandlers = this.subscriptions.get(event);
        if (subscribedHandlers == null)
        {
            this.subscriptions.put(event, subscribedHandlers = new CopyOnWriteArrayList<>());
        }
        subscribedHandlers.add(requestHandler);
    }

    public void unsubscribe(String event, ApiRequestHandler requestHandler)
    {
        assert event != null: "The event name must not be null!";
        assert requestHandler != null: "The request handler must not be null!";
        event = event.toLowerCase(Locale.ENGLISH);

        List<ApiRequestHandler> subscribedHandlers = this.subscriptions.get(event);
        if (subscribedHandlers != null)
        {
            subscribedHandlers.remove(requestHandler);
            if (subscribedHandlers.isEmpty())
            {
                this.subscriptions.remove(event);
            }
        }
    }

    public void unsubscribe(String event)
    {
        assert event != null: "The event name must not be null!";
        event = event.toLowerCase(Locale.ENGLISH);

        this.subscriptions.remove(event);
    }

    public void unsubscribe(ApiRequestHandler handler)
    {
        assert handler != null: "The event name must not be null!";

        Iterator<Map.Entry<String, List<ApiRequestHandler>>> iter = this.subscriptions.entrySet().iterator();

        List<ApiRequestHandler> handlers;
        Iterator<ApiRequestHandler> handlerIter;
        while (iter.hasNext())
        {
            handlers = iter.next().getValue();
            handlerIter = handlers.iterator();
            while (handlerIter.hasNext())
            {
                if (handlerIter.next() == handler)
                {
                    handlerIter.remove();
                }
            }
            if (handlers.isEmpty())
            {
                iter.remove();
            }
        }
    }

    public void fireEvent(String event, Map<String, Object> data)
    {
        assert event != null: "The event name must not be null!";
        event = event.toLowerCase(Locale.ENGLISH);

        List<ApiRequestHandler> subscribedHandlers = this.subscriptions.get(event);
        if (subscribedHandlers != null)
        {
            for (ApiRequestHandler handler : subscribedHandlers)
            {
                handler.handleEvent(event, data);
            }
        }
    }
}
