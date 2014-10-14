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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.Flags;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.command.methodic.parametric.Desc;
import de.cubeisland.engine.command.methodic.parametric.Index;
import de.cubeisland.engine.command.methodic.parametric.Label;
import de.cubeisland.engine.command.methodic.parametric.Optional;
import de.cubeisland.engine.command.methodic.parametric.Reader;
import de.cubeisland.engine.command.old.ReaderException;
import de.cubeisland.engine.command.old.Restricted;
import de.cubeisland.engine.command.old.TooFewArgumentsException;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.command.CommandContainer;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.annotation.CommandPermission;
import de.cubeisland.engine.core.command.annotation.Unloggable;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserList;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.logging.LogLevel;

import static de.cubeisland.engine.core.permission.PermDefault.TRUE;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

@Command(name = "cubeengine", desc = "These are the basic commands of the CubeEngine.", alias = "ce")
public class CoreCommands extends CommandContainer
{
    private final BukkitCore core;
    private final BanManager banManager;
    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();
    private final UserManager um;

    public CoreCommands(Core core)
    {
        super(core.getModuleManager().getCoreModule());
        this.core = (BukkitCore)core;
        this.banManager = core.getBanManager();
        this.um = core.getUserManager();
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandContext context)
    {
        context.sendTranslated(POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();
        PluginManager pm = this.core.getServer().getPluginManager();
        pm.disablePlugin(this.core);
        pm.enablePlugin(this.core);
        context.sendTranslated(POSITIVE, "CubeEngine Reload completed in {integer#time}ms!", System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    @Flags(@Flag(name = "f", longName = "file"))
    public void reloadmodules(CommandContext context)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        context.getCore().getModuleManager().reloadModules(context.hasFlag("f"));
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    @Params(positional = {@Param(label = "password"),
                          @Param(label = "player", type = User.class, req = false)})
    public void setPassword(CommandContext context)
    {
        User target;
        if (context.hasPositional(1))
        {
            target = context.get(1);
        }
        else if (context.getSource() instanceof User)
        {
            target = (User)context.getSource();
        }
        else
        {
            throw new TooFewArgumentsException();
        }
        if (!(context.getSource() == target))
        {
            context.ensurePermission(core.perms().COMMAND_SETPASSWORD_OTHER);
            um.setPassword(target, context.getString(0));
            context.sendTranslated(POSITIVE, "{user}'s password has been set!", target);
        }
        else
        {
            um.setPassword(target, context.getString(0));
            context.sendTranslated(POSITIVE, "Your password has been set!");
        }
    }

    @Command(alias = "clearpw", desc = "Clears your password.")
    public void clearPassword(CommandContext context,
          @Index
          @Optional
          @Label("player")
          @Desc("A List of Players delimited by , or *")
          UserList users)
    {
        CommandSender sender = context.getSource();
        if (users != null)
        {
            if (users.isAll())
            {
                context.ensurePermission(core.perms().COMMAND_CLEARPASSWORD_ALL);
                um.resetAllPasswords();
                sender.sendTranslated(POSITIVE, "All passwords reset!");
                return;
            }
            User target = context.get(0);
            if (!target.equals(context.getSource()))
            {
                context.ensurePermission(core.perms().COMMAND_CLEARPASSWORD_OTHER);
            }
            this.um.resetPassword(target);
            sender.sendTranslated(POSITIVE, "{user}'s password has been reset!", target.getName());
            return;
        }
        if (!(sender instanceof User))
        {
            throw new TooFewArgumentsException();
        }
        this.um.resetPassword((User)sender);
        sender.sendTranslated(POSITIVE, "Your password has been reset!");
    }

    @Unloggable
    @Command(desc = "Logs you in with your password!")
    @Params(positional = @Param(label = "password"))
    @CommandPermission(permDefault = TRUE)
    public void login(CommandContext context)
    {
        CommandSender sender = context.getSource();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (user.isLoggedIn())
            {
                context.sendTranslated(POSITIVE, "You are already logged in!");
                return;
            }
            boolean isLoggedIn = um.login(user, context.getString(0));
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
    @Restricted(value = User.class, msg = "You might use /stop for this.")
    public void logout(CommandContext context)
    {
        User sender = (User)context.getSource();
        if (sender.isLoggedIn())
        {
            sender.logout();
            context.sendTranslated(POSITIVE, "You're now logged out.");
            return;
        }
        context.sendTranslated(NEUTRAL, "You're not logged in!");
    }

    @Command(desc = "Toggles the online mode")
    public void onlinemode(CommandContext context)
    {
        context.sendTranslated(NEGATIVE, "Not working!");
        /*
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
        */
    }

    @Command(desc = "Changes or displays the log level of the server.")
    @Params(positional = @Param(label = "loglevel", type = LogLevel.class, req = false))
    public void loglevel(CommandContext context)
    {
        if (context.hasPositional(0))
        {
            context.getCore().getLog().setLevel(context.<LogLevel>get(0));
            context.sendTranslated(POSITIVE, "New log level successfully set!");
            return;
        }
        context.sendTranslated(NEUTRAL, "The current log level is: {input#loglevel}", context.getCore().getLog().getLevel().getName());
    }

    @Command(alias = "finduser", desc = "Searches for a user in the database")
    @Params(positional = @Param(label = "name"))
    public void searchuser(CommandContext context,
        @Index
        @Reader(FindUserReader.class)
        @Label("name")
        @Desc("The name to search for")
        User user)
    {
        if (user.getName().equalsIgnoreCase(context.getString(0)))
        {
            context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", user);
        }
        else
        {
            context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", user);
        }
    }

    public static class FindUserReader implements ArgumentReader<User>
    {
        @Override
        public User read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
        {
            String name = invocation.consume(1);
            UserManager um = CubeEngine.getCore().getUserManager();
            User found = um.findExactUser(name);
            if (found == null)
            {
                found = um.findUser(name);
            }
            if (found == null)
            {
                throw new ReaderException(CubeEngine.getI18n().translate(NEGATIVE, "No match found for {input}!", name));
            }
            return found;
        }
    }
}
