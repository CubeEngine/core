package de.cubeisland.cubeengine.core.webapi.server;

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
    private final int maxContentLength;
    private final boolean compress;
    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;

    public ApiServerIntializer(int maxContentLength, boolean compress, int compressionLevel, int windowBits, int memLevel)
    {
        this.maxContentLength = maxContentLength;
        this.compress = compress;
        this.compressionLevel = Math.max(1, Math.min(9, compressionLevel));
        this.windowBits = Math.max(9, Math.min(15, windowBits));
        this.memLevel = Math.max(1, Math.min(9, memLevel));
    }
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ch.pipeline()
            .addLast("decoder", new HttpRequestDecoder()) 
            .addLast("aggregator", new HttpChunkAggregator(this.maxContentLength))
            .addLast("encoder", new HttpResponseEncoder())
            .addLast("handler", new ApiServerHandler());
        
        if (this.compress)
        {
            ch.pipeline().addLast("deflater", new HttpContentCompressor(this.compressionLevel, this.windowBits, this.memLevel));
        }
    }
}
