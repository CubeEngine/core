package de.cubeisland.cubeengine.core.webapi.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * This class represents the API server and provides methods to configure and
 * controll it
 *
 * @since 1.0.0
 */
public final class ApiServer
{
    private static ApiServer instance = null;
    private short port;
    private int maxContentLength;
    private String authenticationKey;
    private InetAddress ip;
    private Channel channel;
    private ChannelFactory channelFactory;
    private ServerBootstrap bootstrap;

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
            // error("Could not receive the localhost..."); -- TODO fix logging
        }

        this.bootstrap = null;
        this.channel = null;
        this.channelFactory = null;
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
        return (this.channel != null);
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
    public ApiServer start()
    {
        if (!this.isRunning())
        {
            this.channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());


            this.bootstrap = new ServerBootstrap(this.channelFactory);
            this.bootstrap.setPipelineFactory(new ApiServerPipelineFactory());

            this.channel = bootstrap.bind(new InetSocketAddress(this.ip, this.port));
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
                // error("Shutting down the server was interrupted!"); -- TODO fix logging
                // error("Cleaning up as much as possible..."); -- TODO fix logging
            }
            this.channel = null;
            this.channelFactory.releaseExternalResources();
            this.channelFactory = null;
            this.bootstrap.releaseExternalResources();
            this.bootstrap = null;
        }
        return this;
    }
}