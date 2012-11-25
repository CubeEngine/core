package de.cubeisland.cubeengine.core.util.chatlayout;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractChatLayout<T> implements ChatLayout<T>
{
    public static final int MAX_CHAT_WIDTH = 55;
    protected T data = null;
    protected String[] compiled = null;

    public void setData(T data)
    {
        this.data = data;
        this.compiled = null;
    }

    public T getData()
    {
        return data;
    }

    public List<String> splitUp(String string, int maxLen)
    {
        List<String> parts = new LinkedList<String>();
        if (string == null)
        {
            return null;
        }
        while (string.length() > maxLen)
        {
            parts.add(string.substring(0, maxLen));
            string = string.substring(maxLen);
        }
        if (!string.isEmpty())
        {
            parts.add(string);
        }

        return parts;
    }
}
