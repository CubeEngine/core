package de.cubeisland.engine.core.util.formatter;

import java.util.Locale;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.formatter.MessageCompositor;
import de.cubeisland.engine.formatter.context.FormatContext;
import de.cubeisland.engine.formatter.formatter.Formatter;

public class CubeCompositor extends MessageCompositor
{

    private final char BASE_CHAR = '\u00A7';

    public final String composeMessage(MessageType type, Locale locale, String message, Object[] params)
    {
        return this.composeMessage(locale, type.getColorCode() + message, params);
    }

    @Override
    protected void postFormat(Formatter formatter, FormatContext context, Object messageArgument, StringBuilder finalString)
    {
        if (finalString.length() > 2 && BASE_CHAR == finalString.charAt(0))
        {
            ChatFormat byChar = ChatFormat.getByChar(finalString.charAt(1));
            if (byChar == null)
            {
                return;
            }
            finalString.append(byChar);
        }
    }
}
