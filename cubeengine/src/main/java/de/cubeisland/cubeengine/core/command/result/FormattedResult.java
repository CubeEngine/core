package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.util.chatlayout.ChatLayout;

public class FormattedResult implements CommandResult
{
    private final ChatLayout layout;

    public FormattedResult(ChatLayout layout)
    {
        this.layout = layout;
    }

    @Override
    public void show(CommandContext context)
    {
        for (String line : this.layout.compile())
        {
            context.sendMessage(line);
        }
    }
}
