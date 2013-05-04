package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.OutputStream;

import de.cubeisland.cubeengine.core.config.Configuration;

public class NBTCodecContainer extends CodecContainer<NBTCodecContainer,NBTCodec>
{
    public NBTCodecContainer(NBTCodec codec)
    {
        super(codec);
    }

    public NBTCodecContainer(NBTCodecContainer superContainer, String parentPath)
    {
        super(superContainer, parentPath);
    }

    @Override
    protected void writeConfigToStream(OutputStream stream, Configuration config) throws IOException
    {
    }
}
