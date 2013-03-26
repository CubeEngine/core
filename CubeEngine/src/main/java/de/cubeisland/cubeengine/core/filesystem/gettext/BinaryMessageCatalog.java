package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class BinaryMessageCatalog implements MessageCatalog
{
    private final File file;
    protected static final int HEADER_SIZE = 28;
    protected static final int SIGNATURE_BIG = 0x950412DE;
    protected static final int SIGNATURE_LITTLE = 0xDE120495;

    public BinaryMessageCatalog(File file)
    {
        this.file = file;
    }

    @Override
    public Map<String, String> read() throws IOException
    {
        return this.read(new FileInputStream(this.file));
    }

    @Override
    public Map<String, String> read(InputStream inputStream) throws IOException
    {
        ReadableByteChannel channel;
        if (inputStream instanceof FileInputStream)
        {
            channel = ((FileInputStream)inputStream).getChannel();
        }
        else
        {
            channel = Channels.newChannel(inputStream);
        }
        ByteBuffer buf = ByteBuffer.allocateDirect(HEADER_SIZE);

        int bytesRead = channel.read(buf);
        if (bytesRead < HEADER_SIZE)
        {
            throw new IOException("This binary file is not a valid message catalog: Invalid signature!");
        }
        buf.rewind();

        int signature = buf.getInt();
        if (signature == SIGNATURE_LITTLE)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        }
        else if (signature == SIGNATURE_BIG)
        {
            buf.order(ByteOrder.BIG_ENDIAN);
        }
        else
        {
            throw new IOException("This binary file is not a valid message catalog: Invalid signature!");
        }
        int revision = buf.getInt();
        if (revision != 0)
        {
            throw new IOException("This binary file is not a valid message catalog: Invalid revision!");
        }
        int count = buf.getInt();
        int sourceOffset = buf.getInt();
        int targetOffset = buf.getInt();
        int hashingTableSize = buf.getInt();
        int hashingTableOffset = buf.getInt();

        TIntIntMap lengths = new TIntIntHashMap();

        buf.limit(sourceOffset - HEADER_SIZE);
        channel.read(buf);

        for (int i = 0; i < count; ++i)
        {

        }


        channel.close();

        return null;
    }

    @Override
    public void write(Map<String, String> messages) throws IOException
    {

    }

    @Override
    public void write(OutputStream outputStream, Map<String, String> messages) throws IOException
    {

    }
}
