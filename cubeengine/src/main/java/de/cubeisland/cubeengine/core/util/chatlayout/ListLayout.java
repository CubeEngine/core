package de.cubeisland.cubeengine.core.util.chatlayout;

import de.cubeisland.cubeengine.core.util.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ListLayout extends AbstractChatLayout<List<String>>
{
    private final String bullet;

    public ListLayout()
    {
        this(" - ");
    }

    public ListLayout(String bullet)
    {
        this.bullet = bullet;
    }

    @Override
    public String[] compile()
    {
        if (this.compiled != null)
        {
            return this.compiled;
        }
        if (this.data == null)
        {
            throw new IllegalStateException("No data set yet!");
        }

        final int maxLen = MAX_CHAT_WIDTH - this.bullet.length();
        List<String> lines = new LinkedList<String>();
        final String spacer = StringUtils.repeat(" ", this.bullet.length());

        for (String entry : data)
        {
            List<String> parts = this.splitUp(entry, maxLen);
            Iterator<String> iter = parts.iterator();
            lines.add(this.bullet + iter.next());
            while (iter.hasNext())
            {
                lines.add(spacer + iter.next());
            }
        }

        return this.compiled = lines.toArray(new String[lines.size()]);
    }
}
