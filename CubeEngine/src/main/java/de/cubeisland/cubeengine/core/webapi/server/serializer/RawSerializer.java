package de.cubeisland.cubeengine.core.webapi.server.serializer;

import de.cubeisland.cubeengine.core.webapi.server.ApiResponseSerializer;
import de.cubeisland.cubeengine.core.webapi.server.MimeType;

/**
 *
 * @author Phillip Schichtel
 */
public class RawSerializer implements ApiResponseSerializer
{
    @Override
    public String getName()
    {
        return "raw";
    }

    @Override
    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }

    @Override
    public String serialize(Object o)
    {
        return String.valueOf(o);
    }
}