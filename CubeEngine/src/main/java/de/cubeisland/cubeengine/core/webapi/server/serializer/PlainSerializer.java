package de.cubeisland.cubeengine.core.webapi.server.serializer;

import de.cubeisland.cubeengine.core.webapi.server.ApiResponseSerializer;
import de.cubeisland.cubeengine.core.webapi.server.ApiSerializable;
import de.cubeisland.cubeengine.core.webapi.server.MimeType;
import java.util.Iterator;
import java.util.Map;

public class PlainSerializer implements ApiResponseSerializer
{
    @Override
    public String getName()
    {
        return "plain";
    }

    @Override
    public MimeType getMime()
    {
        return MimeType.PLAIN;
    }

    @Override
    public String serialize(Object o)
    {
        StringBuilder buffer = new StringBuilder();
        this.serialize(buffer, o);
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    private void serialize(StringBuilder buffer, Object o)
    {
        if (o == null)
        {} // null => nothing
        else if (o instanceof ApiSerializable)
        {
            this.serialize(buffer, ((ApiSerializable)o).serialize());
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>)o;
            int dataSize = data.size();
            int counter = 0;
            for (Map.Entry entry : data.entrySet())
            {
                ++counter;
                Object value = entry.getValue();
                this.serialize(buffer, value);
                if (counter < dataSize)
                {
                    buffer.append(',');
                }
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>)o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                this.serialize(buffer, value);
                if (iter.hasNext())
                {
                    buffer.append(',');
                }
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[])o;
            int end = data.length - 1;
            for (int i = 0; i < data.length; i++)
            {
                this.serialize(buffer, data[i]);
                if (i < end)
                {
                    buffer.append(',');
                }
            }
        }
        else
        {
            buffer.append(encode(String.valueOf(o)));
        }
    }

    private static String encode(String string)
    {
        return string.replace("%", "%25").replace(",", "%2C");
    }
}