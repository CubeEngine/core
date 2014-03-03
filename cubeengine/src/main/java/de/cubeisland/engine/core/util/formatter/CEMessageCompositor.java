package de.cubeisland.engine.core.util.formatter;

import de.cubeisland.engine.formatter.MessageCompositor;
import de.cubeisland.engine.formatter.context.FormatContext;
import de.cubeisland.engine.formatter.formatter.Formatter;

public class CEMessageCompositor extends MessageCompositor
{
    private MessageType messageType;

    public CEMessageCompositor(MessageType messageType)
    {
        this.messageType = messageType;
    }

    @Override
    protected String format(Formatter formatter, FormatContext context, Object messageArgument)
    {
        return super.format(formatter, context, messageArgument) + messageType.getColorCode();
    }
}
