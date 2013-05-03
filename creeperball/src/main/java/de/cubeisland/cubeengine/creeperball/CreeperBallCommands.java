package de.cubeisland.cubeengine.creeperball;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;

public class CreeperBallCommands extends ContainerCommand
{
    private User setupUser;

    public CreeperBallCommands(Module module)
    {
        super(module, "creeperball", "All you need to setup your creeperball-game");
        this.registerAlias(new String[]{"cball"},new String[]{});
    }

    @Command(desc = "Setups your creeperball-game",usage = "[saved]")
    public void setup(CommandContext context)
    {
        if (context.hasArg(0))
        {
            // TODO loading from a file
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                if (this.setupUser != null)
                {
                    context.sendTranslated("&2%s&e is already setting up a game!", setupUser.getName());
                    return;
                }
                context.sendTranslated("&aCreeperball Setup Start!");
                this.startSetup((User)context.getSender());
            }
            else
            {
                context.sendTranslated("&cAs console you cannot setup a new game!");
            }
        }
    }

    private void startSetup(User user)
    {
    }
}
