package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.CubeEngine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.ERROR;

/**
 * This class represents the API server and provides methods to configure and
 * controll it
 *
 * @since 1.0.0
 */
public final class ApiServer
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private static ApiServer instance = null;
    private int maxContentLength;
    private boolean compress;
    private int compressionLevel;
    private int windowBits;
    private int memLevel;
    private String authenticationKey;
    
    private InetAddress ip;
    private short port;
    private ServerBootstrap bootstrap;
    private Channel channel;

    private ApiServer()
    {
        this.port = 6561;
        this.maxContentLength = 1048576;
        try
        {
            this.ip = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            LOGGER.log(ERROR, "Could not receive the localhost...");
        }

        this.bootstrap = null;
    }

    /**
     * Returns the singlton instance of the ApiServer
     *
     * @return the ApiServer instance
     */
    public static ApiServer getInstance()
    {
        if (instance == null)
        {
            instance = new ApiServer();
        }
        return instance;
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

    /**
     * Returns the address the server is bound/will bind to
     *
     * @return the address
     */
    public InetAddress getIp()
    {
        return this.ip;
    }

    /**
     * Sets the address the server will bind to on the next start
     *
     * @param ip the address
     * @return fluent interface
     */
    public ApiServer setIp(InetAddress ip)
    {
        if (ip != null)
        {
            this.ip = ip;
        }
        return this;
    }

    /**
     * Returns the port the server is/will be listening on
     *
     * @return the post
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * Sets the port to listen on after the next start
     *
     * @param port the port
     * @return fluent interface
     */
    public ApiServer setPort(short port)
    {
        this.port = port;
        return this;
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

    /**
     * Sets the maximum content length the clients may send after the next start
     *
     * @param maxContentLength the maximum content length
     * @return fluent interface
     */
    public ApiServer setMaxContentLength(int maxContentLength)
    {
        this.maxContentLength = maxContentLength;
        return this;
    }

    /**
     * Returns the authentication key
     *
     * @return the key
     */
    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    /**
     * Sets the authentication key which will be used instantly
     *
     * @param the key
     * @return fluent interface
     */
    public ApiServer setAuthenticationKey(String authkey)
    {
        this.authenticationKey = authkey;
        return this;
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
                    .childHandler(new ApiServerIntializer(this.maxContentLength, this.compress, this.compressionLevel, this.windowBits, this.memLevel))
                    .localAddress(this.ip, this.port);
                
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
}