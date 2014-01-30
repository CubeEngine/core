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
package de.cubeisland.engine.core.command.commands;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.logging.LogLevel;

import static java.util.Arrays.asList;

public class CoreCommands extends ContainerCommand
{

    private final BukkitCore core;
    private final BanManager banManager;
    private final ConcurrentHashMap<String, Long> fails = new ConcurrentHashMap<>();

    public CoreCommands(Core core)
    {
        super(core.getModuleManager().getCoreModule(), "cubeengine", "These are the basic commands of the CubeEngine.", asList("ce"));
        this.core = (BukkitCore)core;
        this.setGeneratePermission(true);
        this.banManager = core.getBanManager();
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandContext context)
    {
        context.sendTranslated("&aReloading CubeEngine! This may take some time...");
        Profiler.startProfiling("ceReload");
        PluginManager pm = this.core.getServer().getPluginManager();
        pm.disablePlugin(this.core);
        pm.enablePlugin(this.core);
        long time = Profiler.endProfiling("ceReload", TimeUnit.MILLISECONDS);
        context.sendTranslated("&aCubeEngine-Reload completed in &6%d&ams!",time);
    }

    @Command(desc = "Reloads all of the modules!", usage = "[-f]", flags = @Flag(name = "f", longName = "file"))
    public void reloadmodules(ParameterizedContext context)
    {
        context.sendTranslated("&aReloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        context.getCore().getModuleManager().reloadModules(context.hasFlag("f"));
        long time = Profiler.endProfiling("modulesReload", TimeUnit.MILLISECONDS);
        context.sendTranslated("&aModules-Reload completed in &6%d&ams!",time);
    }

    @Command(names = {
        "setpassword", "setpw"
    }, desc = "Sets your password.", min = 1, max = 2, usage = "<password> [player]", loggable = false)
    public void setPassword(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target;
        if (context.hasArg(1))
        {
            target = context.getUser(1);
            if (target == null)
            {
                sender.sendTranslated("&cUser %s not found!");
                return;
            }
        }
        else if (sender instanceof User)
        {
            target = (User)sender;
        }
        else
        {
            sender.sendTranslated("&cNo user given!");
            return;
        }

        if (target == sender && !sender.isAuthorized(core.perms().COMMAND_SETPASSWORD_OTHER))
        {
            context.sendTranslated("&cYou are not allowed to change the password of an other user!");
            return;
        }
        core.getUserManager().setPassword(target, context.getString(0));
        if (sender == target)
        {
            sender.sendTranslated("&aThe user's password has been set!");
        }
        else
        {
            sender.sendTranslated("&aYour password has been set!");
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
            if (core.perms().COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
            {
                final UserManager um = this.getModule().getCore().getUserManager();
                um.resetAllPasswords();
                sender.sendTranslated("&All passwords reset!");
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to clear all passwords!");
            }
        }
        else if (context.hasArg(0))
        {
            if (!core.perms().COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to clear the password of other users!");
                return;
            }
            User target = context.getUser(0);
            if (target != null)
            {
                this.core.getUserManager().resetPassword(target);
                sender.sendTranslated("&aThe user's password has been reset!");
            }
            else
            {
                context.sendTranslated("&cUser &c not found!");
            }
        }
        else if (sender instanceof User)
        {
            this.core.getUserManager().resetPassword((User)sender);
            sender.sendTranslated("Your password has been reset!");
        }
    }

    @Command(desc = "Logs you in with your password!", usage = "<password>", min = 1, max = 1, permDefault = PermDefault.TRUE, loggable = false)
    public void login(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (user.isLoggedIn())
            {
                context.sendTranslated("&aYou are already logged in!");
                return;
            }
            boolean isLoggedIn = core.getUserManager().login(user, context.getString(0));
            if (isLoggedIn)
            {
                user.sendTranslated("&aYou logged in successfully!");
            }
            else
            {
                user.sendTranslated("&cWrong password!");
                if (this.core.getConfiguration().security.fail2ban)
                {
                    if (fails.get(user.getName()) != null)
                    {
                        if (fails.get(user.getName()) + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis())
                        {
                            String msg = user.translate("&cToo many wrong passwords! \nFor your security you were banned 10 seconds.");
                            this.banManager.addBan(new UserBan(user.getName(),user.getName(),msg,
                                 new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.core.getConfiguration().security.banDuration))));
                            if (!Bukkit.getServer().getOnlineMode())
                            {
                                this.banManager.addBan(new IpBan(user.getAddress().getAddress(),user.getName(),msg,
                                       new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.core.getConfiguration().security.banDuration))));
                            }
                            user.kickPlayer(msg);
                        }
                    }
                    fails.put(user.getName(),System.currentTimeMillis());
                }
            }
        }
        else
        {
            sender.sendTranslated("&cOnly players can log in!");
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
                sender.sendTranslated("&eYou're not logged in!");
            }
            else
            {
                user.logout();
                sender.sendTranslated("&aYou're now logged out.");
            }
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            sender.sendTranslated("&eYou might use /stop for this.");
        }
    }

    @Command(desc = "Toggles the online mode")
    public void onlinemode(CommandContext context)
    {
        final boolean newState = !this.core.getServer().getOnlineMode();
        BukkitUtils.setOnlineMode(newState);

        if (newState)
        {
            context.sendTranslated("&aThe server is now in online-mode.");
        }
        else
        {
            context.sendTranslated("&aThe server is not in offline-mode.");
        }
    }

    @Command(desc = "Changes or displays the log level of the server.", usage = "[log level]")
    public void loglevel(CommandContext context)
    {
        if (context.hasArgs())
        {
            LogLevel level = LogLevel.toLevel(context.getString(0));
            if (level != null)
            {
                context.getCore().getLog().setLevel(level);
                context.sendTranslated("&aNew log level successfully set!");
            }
            else
            {
                context.sendTranslated("&cThe given log level is unknown.");
            }
        }
        else
        {
            context.sendTranslated("&eThe current log level: &a%s", context.getCore().getLog().getLevel());
        }
    }

    @Command(desc = "Searches for a user in the database", usage = "<name>", min = 1, max = 1, async = true)
    public CommandResult searchUser(CommandContext context)
    {
        final boolean exact = core.getUserManager().getUser(context.getString(0)) != null;
        final User user = core.getUserManager().findUser(context.getString(0), true);
        return new CommandResult()
        {
            @Override
            public void show(CommandContext context)
            {
                if (user == null)
                {
                    context.sendTranslated("&eNo match found for &6%s&e!", context.getString(0));
                }
                else if (exact)
                {
                    context.sendTranslated("&aMatched exactly! User: &2%s", user.getName());
                }
                else
                {
                    context.sendTranslated("&aMatched not exactly! User: &2%s", user.getName());
                }
            }
        };
    }
}
