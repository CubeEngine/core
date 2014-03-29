package de.cubeisland.engine.core.util.formatter;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.messagecompositor.macro.MacroContext;

public class BooleanFormatter extends ColoredFormatter<Boolean>
{
    public BooleanFormatter()
    {
        super(toSet("bool"));
    }

    @Override
    public String process(ChatFormat color, Boolean object, MacroContext context)
    {
        if (color == null)
        {
            color = ChatFormat.GOLD;
        }
        return color + String.valueOf(object);
    }
}
