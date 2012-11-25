package de.cubeisland.cubeengine.core.util.chatlayout;

import de.cubeisland.cubeengine.core.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class TwoColumnLayout extends AbstractChatLayout<List<String[]>>
{
    private final int columnWidth;
    private final String emptyCell;

    public TwoColumnLayout()
    {
        this.columnWidth = (MAX_CHAT_WIDTH - 1) / 2;
        this.emptyCell = StringUtils.repeat(' ', this.columnWidth);
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


        List<String> lines = new LinkedList<String>();

        for (String[] entry : this.data)
        {
            if (entry.length > 1)
            {
                List<String> leftParts = this.splitUp(entry[0], columnWidth);
                List<String> rightParts = this.splitUp(entry[1], columnWidth);

                int diff = leftParts.size() - rightParts.size();
                if (diff > 0)
                {
                    for (; diff >= 0; --diff)
                    {
                        rightParts.add(this.emptyCell);
                    }
                }
                else if (diff < 0)
                {
                    for (; diff <= 0; ++diff)
                    {
                        leftParts.add(this.emptyCell);
                    }
                }

                final int size = leftParts.size();
                for (int i = 0; i < size; ++i)
                {
                    lines.add(StringUtils.padRight(leftParts.get(i), this.columnWidth + 1) + rightParts.get(i));
                }
            }
        }

        return this.compiled = lines.toArray(new String[lines.size()]);
    }
}
