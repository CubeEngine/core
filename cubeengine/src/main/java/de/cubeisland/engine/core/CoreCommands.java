/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.CallAsync;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.CommandPermission;
import de.cubeisland.engine.core.command.reflected.Unloggable;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.logging.LogLevel;

import static de.cubeisland.engine.core.permission.PermDefault.TRUE;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.util.Arrays.asList;

public class CoreCommands extends ContainerCommand
{

    private final BukkitCore core;
    private final BanManager banManager;
    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();

    public CoreCommands(Core core)
    {
        super(core.getModuleManager().getCoreModule(), "cubeengine", "These are the basic commands of the CubeEngine.");
        this.setAliases(new HashSet<>(asList("ce")));
        this.core = (BukkitCore)core;
        this.banManager = core.getBanManager();
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandContext context)
    {
        context.sendTranslated(POSITIVE, "Reloading CubeEngine! This may take some time...");
        Profiler.startProfiling("ceReload");
        PluginManager pm = this.core.getServer().getPluginManager();
        pm.disablePlugin(this.core);
        pm.enablePlugin(this.core);
        long time = Profiler.endProfiling("ceReload", TimeUnit.MILLISECONDS);
        context.sendTranslated(POSITIVE, "CubeEngine Reload completed in {integer#time}ms!", time);
    }

    @Command(desc = "Reloads all of the modules!")
    @Flags(@Flag(name = "f", longName = "file"))
    public void reloadmodules(ParameterizedContext context)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        context.getCore().getModuleManager().reloadModules(context.hasFlag("f"));
        long time = Profiler.endProfiling("modulesReload", TimeUnit.MILLISECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}ms!", time);
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    @IParams({@Grouped(@Indexed(label = "password")),
              @Grouped(value = @Indexed(label = "player", type = User.class), req = false)})
    public void setPassword(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target;
        if (context.hasArg(1))
        {
            target = context.getArg(1);
            if (target == null)
            {
                sender.sendTranslated(NEGATIVE, "Player {user} not found!");
                return;
            }
        }
        else if (sender instanceof User)
        {
            target = (User)sender;
        }
        else
        {
            sender.sendTranslated(NEGATIVE, "No player given!");
            return;
        }

        if (target == sender && !sender.isAuthorized(core.perms().COMMAND_SETPASSWORD_OTHER))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to change the password of an other player!");
            return;
        }
        core.getUserManager().setPassword(target, context.<String>getArg(0));
        if (sender == target)
        {
            sender.sendTranslated(POSITIVE, "The player's password has been set!");
        }
        else
        {
            sender.sendTranslated(POSITIVE, "Your password has been set!");
        }
    }

    @Command(alias = "clearpw", desc = "Clears your password.")
    @IParams(@Grouped(value = @Indexed(label = {"player","!*"}, type = {User.class, String.class}), req = false))
    public void clearPassword(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        if (context.hasArg(0))
        {
            if ("*".equals(context.getArg(0)))
            {
                if (core.perms().COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
                {
                    final UserManager um = this.getModule().getCore().getUserManager();
                    um.resetAllPasswords();
                    sender.sendTranslated(POSITIVE, "All passwords reset!");
                    return;
                }
                context.sendTranslated(NEGATIVE, "You are not allowed to clear all passwords!");
                return;
            }
            if (!core.perms().COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to clear the password of other players!");
                return;
            }
            User target = context.getArg(0);
            if (target != null)
            {
                this.core.getUserManager().resetPassword(target);
                sender.sendTranslated(POSITIVE, "The player's password has been reset!");
                return;
            }
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        if (sender instanceof User)
        {
            this.core.getUserManager().resetPassword((User)sender);
            sender.sendTranslated(POSITIVE, "Your password has been reset!");
        }
    }

    @Unloggable
    @Command(desc = "Logs you in with your password!")
    @IParams(@Grouped(@Indexed(label = "password")))
    @CommandPermission(permDefault = TRUE)
    public void login(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (user.isLoggedIn())
            {
                context.sendTranslated(POSITIVE, "You are already logged in!");
                return;
            }
            boolean isLoggedIn = core.getUserManager().login(user, context.<String>getArg(0));
            if (isLoggedIn)
            {
                user.sendTranslated(POSITIVE, "You logged in successfully!");
            }
            else
            {
                user.sendTranslated(NEGATIVE, "Wrong password!");
                if (this.core.getConfiguration().security.fail2ban)
                {
                    if (fails.get(user.getUniqueId()) != null)
                    {
                        if (fails.get(user.getUniqueId()) + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis())
                        {
                            String msg = user.getTranslation(NEGATIVE, "Too many wrong passwords! \nFor your security you were banned 10 seconds.");
                            this.banManager.addBan(new UserBan(user.getName(),user.getName(), msg,
                                 new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.core.getConfiguration().security.banDuration))));
                            if (!Bukkit.getServer().getOnlineMode())
                            {
                                this.banManager.addBan(new IpBan(user.getAddress().getAddress(),user.getName(),msg,
                                       new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.core.getConfiguration().security.banDuration))));
                            }
                            user.kickPlayer(msg);
                        }
                    }
                    fails.put(user.getUniqueId(),System.currentTimeMillis());
                }
            }
        }
        else
        {
            sender.sendTranslated(NEGATIVE, "Only players can log in!");
        }
    }

    @Command(desc = "Logs you out!")
    public void logout(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (!user.isLoggedIn())
            {
                sender.sendTranslated(NEUTRAL, "You're not logged in!");
            }
            else
            {
                user.logout();
                sender.sendTranslated(POSITIVE, "You're now logged out.");
            }
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            sender.sendTranslated(NEUTRAL, "You might use /stop for this.");
        }
    }

    @Command(desc = "Toggles the online mode")
    public void onlinemode(CommandContext context)
    {
        final boolean newState = !this.core.getServer().getOnlineMode();
        BukkitUtils.setOnlineMode(newState);

        if (newState)
        {
            context.sendTranslated(POSITIVE, "The server is now in online-mode.");
        }
        else
        {
            context.sendTranslated(POSITIVE, "The server is not in offline-mode.");
        }
    }

    @Command(desc = "Changes or displays the log level of the server.")
    @IParams(@Grouped(value = @Indexed(label = "loglevel", type = LogLevel.class), req = false))
    public void loglevel(CommandContext context)
    {
        if (context.hasArgs())
        {
            context.getCore().getLog().setLevel(context.<LogLevel>getArg(0));
            context.sendTranslated(POSITIVE, "New log level successfully set!");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The current log level: {input#loglevel}", context.getCore().getLog().getLevel().getName());
        }
    }

    @CallAsync
    @Command(desc = "Searches for a user in the database")
    @IParams(@Grouped(@Indexed(label = "name")))
    public CommandResult searchUser(CommandContext context)
    {
        final boolean exact = core.getUserManager().findExactUser(context.<String>getArg(0)) != null;
        final User user = core.getUserManager().findUser(context.<String>getArg(0), true);
        return new CommandResult()
        {
            @Override
            public void show(CommandContext context)
            {
                if (user == null)
                {
                    context.sendTranslated(NEUTRAL, "No match found for {input}!", context.getArg(0));
                }
                else if (exact)
                {
                    context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", user);
                }
                else
                {
                    context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", user);
                }
            }
        };
    }
}
