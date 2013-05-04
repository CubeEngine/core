package de.cubeisland.cubeengine.core.config.codec;

import java.io.InputStream;

public class NBTCodec extends ConfigurationCodec
{
    @Override
    public String getExtension()
    {
        return "dat";
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
