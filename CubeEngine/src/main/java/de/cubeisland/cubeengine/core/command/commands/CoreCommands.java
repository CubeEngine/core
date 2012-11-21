package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.*;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.core.module.CoreModule;
import de.cubeisland.cubeengine.core.user.User;

public class CoreCommands extends ContainerCommand
{

    private final BukkitCore core;

    public CoreCommands(Core core)
    {
        super(CoreModule.get(), "cubeengine", "These are the basic commands of the CubeEngine.", "ce");
        this.core = (BukkitCore)core;
    }

    @Command(desc = "Disables the CubeEngine")
    public void disable(CommandContext context)
    {
        this.core.getServer().getPluginManager().disablePlugin(this.core);
    }

    @Command(names = {
        "setpassword", "setpw"
    }, desc = "Sets your password.", min = 1, max = 2, usage = "<password> [player]")
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
        context.sendMessage("core", "&aPassword set!");
    }

    @Command(names = {
        "clearpassword", "clearpw"
    }, desc = "Clears your password.", max = 1, usage = "[<player>|-a]", flags = @Flag(longName = "all", name = "a"))
    public void clearPassword(CommandContext context)
    {
        User user;
        if (context.hasFlag("a"))
        {
            if (CommandPermissions.COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
            {
                this.getModule().getUserManager().resetAllPasswords();
                for (User user1 : this.getModule().getUserManager().getLoadedUsers())
                {
                    user1.passwd = null; //update loaded users
                }
                context.sendMessage("core", "&all passwords resetted!");
            }
            else
            {
                denyAccess(context, "core", "&cYou are not allowed to clear all passwords!");
            }
            return;
        }
        else if (context.hasIndexed(0))
        {
            if (!CommandPermissions.COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "core", "&cYou are not allowed to clear the password of other users!");
            }
            user = context.getUser(0);
            if (user == null)
            {
                paramNotFound(context, "core", "&cUser &c not found!");
            }
        }
        else
        {
            user = context.getSenderAsUser("core", "&cYou do not need an ingame password as console!");
        }
        user.resetPassword();
        context.sendMessage("core", "&aPassword reset!");
    }

    @Command(desc = "Loggs you in with your password!", usage = "<password>", min = 1, max = 1)
    public void login(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eYou dont need a password for ingame!");
        if (sender.isLoggedIn())
        {
            blockCommand(context, "core", "&aYou are already logged in!");
        }
        boolean isLoggedIn = sender.login(context.getString(0));
        if (isLoggedIn)
        {
            context.sendMessage("core", "&aYou logged in succesfully!");
        }
        else
        {
            context.sendMessage("core", "&cWrong password!");
        }
    }

    @Command(desc = "Logs you out!", max = 0)
    public void logout(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eJust close the console!");
        if (!sender.isLoggedIn())
        {
            blockCommand(context, "core", "&aYou were not logged in!");
        }
        sender.logout();
        context.sendMessage("core", "&aYou are now logged out!");
    }
}
