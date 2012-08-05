package de.cubeisland.cubeengine.core.webapi.server.serializer;

import de.cubeisland.cubeengine.core.webapi.server.ApiResponseSerializer;
import de.cubeisland.cubeengine.core.webapi.server.ApiSerializable;
import de.cubeisland.cubeengine.core.webapi.server.MimeType;
import java.util.Iterator;
import java.util.Map;

public class XmlSerializer implements ApiResponseSerializer
{
    private final static String XMLDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

    public String getName()
    {
        return "xml";
    }

    public MimeType getMime()
    {
        return MimeType.XML;
    }

    public String serialize(Object o)
    {
        StringBuilder buffer = new StringBuilder();
        this.serialize(buffer, o, "response", true);
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    private void serialize(StringBuilder buffer, Object o, String nodeName, boolean firstLevel)
    {
        if (firstLevel)
        {
            buffer.append(XMLDeclaration);
        }
        buffer.append('<').append(nodeName).append('>');
        if (o == null)
        {
        } // null -> do nothing
        else if (o instanceof ApiSerializable)
        {
            this.serialize(buffer, ((ApiSerializable)o).serialize(), nodeName, false);
        }
        else if (o instanceof Map)
        {
            Map<String, Object> data = (Map<String, Object>)o;
            for (Map.Entry entry : data.entrySet())
            {
                this.serialize(buffer, entry.getValue(), entry.getKey().toString(), false);
            }
        }
        else if (o instanceof Iterable)
        {
            Iterable<Object> data = (Iterable<Object>)o;
            Iterator iter = data.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                this.serialize(buffer, value, nodeName, false);
            }
        }
        else if (o.getClass().isArray())
        {
            Object[] data = (Object[])o;
            for (int i = 0; i < data.length; i++)
            {
                this.serialize(buffer, data[i], nodeName, false);
            }
        }
        else
        {
            buffer.append(escape(String.valueOf(o)));
        }

        buffer.append("</").append(nodeName).append('>');
    }

    private static String escape(String string)
    {
        return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
