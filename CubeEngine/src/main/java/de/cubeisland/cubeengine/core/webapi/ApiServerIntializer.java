package de.cubeisland.cubeengine.core.webapi;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 *
 * @author Phillip Schichtel
 */
public class ApiServerIntializer extends ChannelInitializer<SocketChannel>
{
    private final ApiServer server;

    ApiServerIntializer(ApiServer server)
    {
        this.server = server;
    }
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ch.pipeline()
            .addLast("decoder", new HttpRequestDecoder()) 
            .addLast("aggregator", new HttpChunkAggregator(this.server.getMaxContentLength()))
            .addLast("encoder", new HttpResponseEncoder())
            .addLast("handler", new ApiRequestHandler(this.server));
        
        if (this.server.isCompressionEnabled())
        {
            ch.pipeline().addLast("deflater", new HttpContentCompressor(this.server.getCompressionLevel(), this.server.getCompressionWindowBits(), this.server.getCompressionMemoryLevel()));
        }
    }
}
