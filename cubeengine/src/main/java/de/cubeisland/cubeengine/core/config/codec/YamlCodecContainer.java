package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.util.StringUtils;

public class YamlCodecContainer extends MultiCodecContainer<YamlCodecContainer,YamlCodec>
{
    public YamlCodecContainer(YamlCodec codec)
    {
        super(codec);
    }

    public YamlCodecContainer(YamlCodecContainer superContainer, String parentPath)
    {
        super(superContainer, parentPath);
    }

    @Override
    protected void writeConfigToStream(OutputStreamWriter writer, Configuration config) throws IOException
    {
        if (config.head() != null)
        {
            writer.append("# ").append(StringUtils.implode("\n# ", config.head())).append(codec.LINE_BREAK).append(codec.LINE_BREAK);
        }
        codec.first = true;
        codec.convertMap(writer, this, values);
        if (config.tail() != null)
        {
            writer.append("# ").append(StringUtils.implode("\n# ", config.tail()));
        }
    }
}
