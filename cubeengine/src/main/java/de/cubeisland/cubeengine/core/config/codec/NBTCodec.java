package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import de.cubeisland.cubeengine.core.config.node.MapNode;

public class NBTCodec extends ConfigurationCodec
{
    public static final String extension = "dat";

    @Override
    public void convertMap(OutputStreamWriter writer, CodecContainer container, MapNode values) throws IOException
    {

    }

    @Override
    public String buildComment(CodecContainer container, String path, int off)
    {
        throw new UnsupportedOperationException("Comments are not supported in NBT!");
    }

    @Override
    public String getExtension()
    {
        return extension;
    }

    @Override
    public void loadFromInputStream(CodecContainer container, InputStream is)
    {
    }

    @Override
    protected CodecContainer createCodecContainer()
    {
        return new NBTCodecContainer(this);
    }
}
