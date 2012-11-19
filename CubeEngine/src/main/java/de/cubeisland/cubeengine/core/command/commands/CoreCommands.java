package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.module.CoreModule;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.*;
import de.cubeisland.cubeengine.core.user.User;

public class CoreCommands extends ContainerCommand
{

    private final BukkitCore core;

    public CoreCommands(Core core)
    {
        super(CoreModule.get(), "cubeengine", "These are the basic commands of the CubeEngine.", "ce");
        this.core = (BukkitCore) core;
    }

    @Command(desc = "Disables the CubeEngine")
    public void disable(CommandContext context)
    {
        this.core.getServer().getPluginManager().disablePlugin(this.core);
    }

    @Command(desc = "Sets your password.",
    min = 1,
    max = 2,
    usage = "<password> [player]")
    public void setPassword(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        if (sender == null && !context.hasIndexed(1))
        {
            illegalParameter(context, "core", "&cPlayer missing! Can not set password.");
        }
        User user = sender;
        if (context.hasIndexed(1))
        {
            if (!CommandPermissions.COMMAND_SETPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to change the password of an other user!");
            }
            user = context.getUser(1);
            if (user == null)
            {
                blockCommand(context, "core", "&cUser %s not found!", context.getString(1));
            }
        }
        user.setPassword(context.getString(0));
    }

    @Command(desc = "Clears your password.",
    max = 1,
    usage = "[<player>|-a]",
    flags =
    @Flag(longName = "all", name = "a"))
    public void clearPassword(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            if (CommandPermissions.COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
            {
                this.getModule().getUserManager(); //TODO custom query to remove all pw 
                for (User user : this.getModule().getUserManager().getLoadedUsers())
                {
                    user.passwd = null; //update loaded users
                }
            }
            else
            {
                denyAccess(context, "core", "&cYou are not allowed to clear all passwords!");
            }
        }
        else if (context.hasIndexed(0))
        {
            if (!CommandPermissions.COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "core", "&cYou are not allowed to clear the password of other users!");
            }
            User user = context.getUser(0);
            if (user == null)
            {
                paramNotFound(context, "core", "&cUser &c not found!");
            }
            user.resetPassword();
        }
    }
}
