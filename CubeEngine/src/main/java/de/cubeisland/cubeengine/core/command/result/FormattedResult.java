package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.util.chatlayout.ChatLayout;

public class FormattedResult implements CommandResult
{
    private final ChatLayout layout;

    public FormattedResult(ChatLayout layout)
    {
        this.layout = layout;
    }

    @Override
    public void show(CommandSender sender)
    {
        for (String line : this.layout.compile())
        {
            sender.sendMessage(line);
        }
    }
}
