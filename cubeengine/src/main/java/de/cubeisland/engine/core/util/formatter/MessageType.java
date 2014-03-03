package de.cubeisland.engine.core.util.formatter;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.formatter.formatter.ArgumentSuffix;

public class MessageType implements ArgumentSuffix
{
    public final static MessageType POSITIVE = new MessageType(ChatFormat.BRIGHT_GREEN);
    public final static MessageType NEUTRAL = new MessageType(ChatFormat.YELLOW);
    public final static MessageType NEGATIVE = new MessageType(ChatFormat.RED);
    public final static MessageType CRITICAL = new MessageType(ChatFormat.DARK_RED);
    public final static MessageType NONE = new MessageType(ChatFormat.WHITE);

    private ChatFormat color;

    private MessageType(ChatFormat color)
    {
        this.color = color;
    }

    @Override
    public String getSuffix()
    {
        return this.color.toString();
    }
}
