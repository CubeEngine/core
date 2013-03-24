package de.cubeisland.cubeengine.core.filesystem.gettext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class BinaryMessageCatalog implements MessageCatalog
{
    private final File file;
    private static final int[] MAGIC_NUMBER = {0xde, 0x12, 0x04, 0x95};

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
        int[] magic = new int[4];
        magic[0] = inputStream.read();
        magic[1] = inputStream.read();
        magic[2] = inputStream.read();
        magic[3] = inputStream.read();

        if (!MAGIC_NUMBER.equals(magic))
        {
            int tmp;
            for (int i = 0; i < magic.length / 2; ++i)
            {
                tmp = magic[i];
                magic[i] = magic[magic.length - 1 - i];
                magic[magic.length - 1 - i] = tmp;
            }
            if (!MAGIC_NUMBER.equals(magic))
            {
                throw new RuntimeException("To specified file is NOT a valid binary message catalog.");
            }
        }

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
