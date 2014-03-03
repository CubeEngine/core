package de.cubeisland.engine.core.util.formatter;

import java.util.Locale;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.formatter.DefaultMessageCompositor;
import de.cubeisland.engine.formatter.context.FormatContext;
import de.cubeisland.engine.formatter.formatter.Formatter;

public class ColoredMessageCompositor extends DefaultMessageCompositor
{
    private final char BASE_CHAR = '\u00A7';

    private ColorConfiguration colorConfiguration;

    public ColoredMessageCompositor(Core core)
    {
        this.colorConfiguration = core.getConfigFactory().load(ColorConfiguration.class, core.getFileManager().getDataPath().resolve("formatColor" + FileExtensionFilter.YAML.getExtention()).toFile());
    }

    public String composeMessage(MessageType type, Locale locale, String sourceMessage, Object... messageArgs)
    {
        return this.composeMessage(locale, this.getColorString(type) + sourceMessage, messageArgs);
    }

    @Override
    public void postFormat(Formatter formatter, FormatContext context, Object messageArgument, StringBuilder finalString)
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

    public String getColorString(MessageType type)
    {
        return this.colorConfiguration.colorMap.get(type).toString();
    }
}
