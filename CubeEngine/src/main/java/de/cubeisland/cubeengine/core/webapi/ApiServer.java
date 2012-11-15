package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.webapi.ApiConfig;
import de.cubeisland.cubeengine.core.webapi.exception.ApiStartupException;
import gnu.trove.set.hash.THashSet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.ERROR;
import static java.util.logging.Level.WARNING;

/**
 * This class represents the API server and provides methods to configure and
 * controll it
 */
public class ApiServer
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    private final Core core;
    
    private int maxContentLength;
    private boolean compress;
    private int compressionLevel;
    private int windowBits;
    private int memoryLevel;
    
    private InetAddress bindAddress;
    private short port;
    private ServerBootstrap bootstrap;
    private Channel channel;
    
    private final Set<String> disabledRoutes;
    private boolean enableWhitelist;
    private final Set<String> whitelist;
    private boolean enableBlacklist;
    private final Set<String> blacklist;

    public ApiServer(Core core)
    {
        this.core = core;
        this.bootstrap = null;
        this.channel = null;
        
        try
        {
            this.bindAddress = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ignored)
        {
            LOGGER.log(WARNING, "Failed to get the localhost!");
        }
        this.port = 6561;
        this.maxContentLength = 1048576;

        this.compress = false;
        this.compressionLevel = 9;
        this.windowBits = 15;
        this.memoryLevel = 9;
        
        this.disabledRoutes = new THashSet<String>();
        this.enableWhitelist = false;
        this.whitelist = new THashSet<String>();
        this.enableBlacklist = false;
        this.blacklist = new THashSet<String>();
    }
    
    public void configure(final ApiConfig config)
    {
        Validate.notNull(config, "The config must not be null!");
        
        try
        {
            this.setBindAddress(config.address);
        }
        catch (UnknownHostException ignored)
        {
            LOGGER.log(WARNING, "Failed to resolve the host {0}, ignoring the value...");
        }
        this.setPort(config.port);
        
        this.setCompressionEnabled(config.compression);
        this.setCompressionLevel(config.compressionLevel);
        this.setCompressionWindowBits(config.windowBits);
        this.setCompressionMemoryLevel(config.memoryLevel);
        
        this.setWhitelistEnabled(config.whitelistEnable);
        this.setWhitelist(config.whitelist);
        
        this.setBlacklistEnabled(config.blacklistEnable);
        this.setBlacklist(config.blacklist);
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
            this.bootstrap = new ServerBootstrap();
            
            try
            {
                this.bootstrap.group(new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ApiServerIntializer(this))
                    .localAddress(this.bindAddress, this.port);
                
                this.channel = this.bootstrap.bind().sync().channel();
            }
            catch (Exception e)
            {
                this.bootstrap.shutdown();
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
            try
            {
                this.channel.close().await(5000);
            }
            catch (InterruptedException e)
            {
                LOGGER.log(ERROR, "Shutting down the server was interrupted!");
                LOGGER.log(ERROR, "Cleaning up as much as possible...");
            }
            this.channel = null;
            this.bootstrap.shutdown();
            this.bootstrap = null;
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
        return (this.channel != null && this.channel.isOpen());
    }
    
    public void setBindAddress(String address) throws UnknownHostException
    {
        this.setBindAddress(InetAddress.getByName(address));
    }

    /**
     * Sets the address the server will bind to on the next start
     *
     * @param address the address
     * @return fluent interface
     */
    public void setBindAddress(InetAddress address)
    {
        Validate.notNull(address, "The address must not be null!");
        
        this.bindAddress = address;
    }

    /**
     * Returns the address the server is bound/will bind to
     *
     * @return the address
     */
    public InetAddress getBindAddress()
    {
        return this.bindAddress;
    }
    
    public InetAddress getBoundAddress()
    {
        if (this.isRunning())
        {
            return ((InetSocketAddress)this.channel.localAddress()).getAddress();
        }
        return null;
    }

    public void setPort(short port)
    {
        this.port = port;
    }
    
    /**
     * Returns the port the server is/will be listening on
     *
     * @return the post
     */
    public short getPort()
    {
        return this.port;
    }
    
    public short getBoundPort()
    {
        if (this.isRunning())
        {
            return (short)((InetSocketAddress)this.channel.localAddress()).getPort();
        }
        return -1;
    }

    public void setMaxContentLength(int mcl)
    {
        this.maxContentLength = mcl;
    }

    /**
     * Returns the maximum content length the client may send
     *
     * @return the maximum content length
     */
    public int getMaxContentLength()
    {
        return this.maxContentLength;
    }
    
    public void setCompressionEnabled(boolean state)
    {
        this.compress = state;
    }
    
    public boolean isCompressionEnabled()
    {
        return this.compress;
    }
    
    public void setCompressionLevel(int level)
    {
        this.compressionLevel = Math.max(1, Math.min(9, level));
    }
    
    public int getCompressionLevel()
    {
        return this.compressionLevel;
    }
    
    public void setCompressionWindowBits(int bits)
    {
        this.windowBits = Math.max(9, Math.min(15, bits));
    }
    
    public int getCompressionMemoryLevel()
    {
        return this.memoryLevel;
    }
    
    public int getCompressionWindowBits()
    {
        return this.windowBits;
    }
    
    public void setCompressionMemoryLevel(int level)
    {
        this.memoryLevel = Math.max(1, Math.min(9, level));
    }

    /**
     * Returns whether whitelisting is enabled
     *
     * @return true if enabled
     */
    public boolean isWhitelistEnabled()
    {
        return this.enableWhitelist;
    }

    /**
     * Sets the enabled state of the whitelisting
     *
     * @param state true to enable, false to disable
     * @return fluent interface
     */
    public void setWhitelistEnabled(boolean state)
    {
        this.enableWhitelist = state;
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
        Validate.notNull(newWhitelist, "The whitelist must not be null!");
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
        return this.enableWhitelist ? this.whitelist.contains(ip) : true;
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
     * @return fluent interface
     */
    public void setBlacklistEnabled(boolean state)
    {
        this.enableBlacklist = state;
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
        Validate.notNull(newBlacklist, "The blacklist must not be null!");
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
        return this.enableBlacklist;
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
        return this.enableBlacklist ? this.blacklist.contains(ip) : false;
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
}