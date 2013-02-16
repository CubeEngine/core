package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.plugin.PluginManager;

import static de.cubeisland.cubeengine.core.command.commands.CommandPermissions.COMMAND_SETPASSWORD_OTHER;
import static de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException.paramNotFound;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static java.util.Arrays.asList;

public class CoreCommands extends ContainerCommand
{

    private final BukkitCore core;

    public CoreCommands(Core core)
    {
        super(core.getModuleManager().getCoreModule(), "cubeengine", "These are the basic commands of the CubeEngine.", asList("ce"));
        this.core = (BukkitCore)core;
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandContext context)
    {
        PluginManager pm = this.core.getServer().getPluginManager();
        pm.disablePlugin(this.core);
        pm.enablePlugin(this.core);
    }

    @Command(desc = "Disables the CubeEngine")
    public void reloadmodules(CommandContext context)
    {
        this.core.getModuleManager().unloadModules();
        this.core.getModuleManager().loadModules(this.core.getFileManager().getModulesDir());
    }

    @Command(names = {
        "setpassword", "setpw"
    }, desc = "Sets your password.", min = 1, max = 2, usage = "<password> [player]")
    public void setPassword(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = null;
        if (context.hasArg(1))
        {
            target = context.getUser(1);
            if (target == null)
            {
                sender.sendMessage("core", "&cUser %s not found!");
                return;
            }
        }
        else if (sender instanceof User)
        {
            target = (User)sender;
        }
        else
        {
            sender.sendMessage("core", "&cNo user given!");
            return;
        }

        if (target == sender && !sender.isAuthorized(COMMAND_SETPASSWORD_OTHER))
        {
            denyAccess(context, "core", "&cYou are not allowed to change the password of an other user!");
            return;
        }
        target.setPassword(context.getString(0));
        if (sender == target)
        {
            sender.sendMessage("core", "&aThe user's password has been set!");
        }
        else
        {
            sender.sendMessage("core", "&aYour password has been set!");
        }
    }

    @Command(names = {
        "clearpassword", "clearpw"
    }, desc = "Clears your password.", max = 1, usage = "[<player>|-a]", flags = @Flag(longName = "all", name = "a"))
    public void clearPassword(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        if (context.hasFlag("a"))
        {
            if (CommandPermissions.COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
            {
                this.getModule().getUserManager().resetAllPasswords();
                for (User user : this.getModule().getUserManager().getLoadedUsers())
                {
                    user.passwd = null; //update loaded users
                }
                sender.sendMessage("core", "&All passwords reset!");
            }
            else
            {
                denyAccess(context, "core", "&cYou are not allowed to clear all passwords!");
            }
        }
        else if (context.hasArg(0))
        {
            if (!CommandPermissions.COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "core", "&cYou are not allowed to clear the password of other users!");
            }
            User target = context.getUser(0);
            if (target != null)
            {
                target.resetPassword();
                sender.sendMessage("core", "&aThe user's password has been reset!");
            }
            else
            {
                paramNotFound(context, "core", "&cUser &c not found!");
            }
        }
        else if (sender instanceof User)
        {
            ((User)sender).resetPassword();
            sender.sendMessage("core", "Your password has been reset!");
        }
    }

    @Command(desc = "Loggs you in with your password!", usage = "<password>", min = 1, max = 1)
    public void login(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (user.isLoggedIn())
            {
                blockCommand(context, "core", "&aYou are already logged in!");
            }
            boolean isLoggedIn = user.login(context.getString(0));
            if (isLoggedIn)
            {
                user.sendMessage("core", "&aYou logged in successfully!");
            }
            else
            {
                user.sendMessage("core", "&cWrong password!");
            }
        }
        else
        {
            sender.sendMessage("core", "&cOnly players can log in!");
        }
    }

    @Command(desc = "Logs you out!", max = 0)
    public void logout(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (!user.isLoggedIn())
            {
                sender.sendMessage("core", "&eYou're not logged in!");
            }
            else
            {
                user.logout();
                sender.sendMessage("core", "&aYou're now logged out.");
            }
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            sender.sendMessage("core", "&eYou might use /stop for this.");
        }
    }
}
