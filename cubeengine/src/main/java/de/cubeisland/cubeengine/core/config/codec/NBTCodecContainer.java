package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.OutputStream;

import de.cubeisland.cubeengine.core.config.Configuration;

import org.spout.nbt.stream.NBTOutputStream;

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
        NBTOutputStream nbtOutputStream = new NBTOutputStream(stream, false);
        nbtOutputStream.writeTag(this.codec.convertMap(this));
        nbtOutputStream.flush();
        nbtOutputStream.close();
    }
}
