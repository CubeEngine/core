package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;

import java.util.Arrays;

public class SignMarketCommands extends ContainerCommand
{
    private Signmarket module;

    public SignMarketCommands(Signmarket module)
    {
        super(module, "marketsign", "MarketSign-Commands", Arrays.asList("signmarket","market"));
        this.module = module;
    }

    @Alias(names = "medit")
    @Command(names = {"editMode","edit"},
            desc = "Enters the editmode allowing to change market-signs easily")
    public void editMode(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            if (this.module.getEditModeListener().hasUser((User)context.getSender()))
            {
                this.module.getEditModeListener().removeUser((User)context.getSender());
            }
            else
            {
                this.module.getEditModeListener().addUser((User)context.getSender());
                context.sendTranslated("&aYou are now in edit mode!\n" +
                                           "Chat will now work as commands.\n" +
                                           "&eType exit or use this command again to leave the editmode.");
            }
        }
        else
        {
            context.sendTranslated("&cOnly players can edit marketsigns!");
        }
    }
}
