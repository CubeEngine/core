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
import de.cubeisland.engine.command.filter.Restricted;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.parametric.Default;
import de.cubeisland.engine.command.methodic.parametric.Desc;
import de.cubeisland.engine.command.methodic.parametric.Label;
import de.cubeisland.engine.command.methodic.parametric.Optional;
import de.cubeisland.engine.command.methodic.parametric.Reader;
import de.cubeisland.engine.command.parameter.TooFewArgumentsException;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
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
import de.cubeisland.engine.logscribe.LogLevel;

import static de.cubeisland.engine.core.permission.PermDefault.TRUE;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

@Command(name = "cubeengine", alias = "ce",
         desc = "These are the basic commands of the CubeEngine.")
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

        core.getCommandManager().getReaderManager().registerReader(new FindUserReader());
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
    public void reloadmodules(CommandContext context, @Flag(name = "f", longName = "file") boolean fromFile)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        context.getCore().getModuleManager().reloadModules(fromFile);
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    public void setPassword(CommandContext context, @Label("password") String password, @Default @Label("player") User target)
    {
        if ((context.getSource().equals(target)))
        {
            um.setPassword(target, context.getString(0));
            context.sendTranslated(POSITIVE, "Your password has been set!");
            return;
        }
        context.ensurePermission(core.perms().COMMAND_SETPASSWORD_OTHER);
        um.setPassword(target, context.getString(0));
        context.sendTranslated(POSITIVE, "{user}'s password has been set!", target);
    }

    @Command(alias = "clearpw", desc = "Clears your password.")
    public void clearPassword(CommandContext context,
          @Optional @Label("players") @Desc("* or a list of Players delimited by ,") UserList users)
    {
        CommandSender sender = context.getSource();
        if (users == null)
        {
            if (!(sender instanceof User))
            {
                throw new TooFewArgumentsException();
            }
            this.um.resetPassword((User)sender);
            sender.sendTranslated(POSITIVE, "Your password has been reset!");
            return;
        }
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
    }

    @Unloggable
    @Command(desc = "Logs you in with your password!")
    @CommandPermission(permDefault = TRUE)
    @Restricted(value = User.class, msg = "Only players can log in!")
    public void login(CommandContext context, @Label("password") String password)
    {
        User user = (User)context.getSource();
        if (user.isLoggedIn())
        {
            context.sendTranslated(POSITIVE, "You are already logged in!");
            return;
        }
        boolean isLoggedIn = um.login(user, password);
        if (isLoggedIn)
        {
            user.sendTranslated(POSITIVE, "You logged in successfully!");
            return;
        }
        user.sendTranslated(NEGATIVE, "Wrong password!");
        if (this.core.getConfiguration().security.fail2ban)
        {
            if (fails.get(user.getUniqueId()) != null)
            {
                if (fails.get(user.getUniqueId()) + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis())
                {
                    String msg = user.getTranslation(NEGATIVE, "Too many wrong passwords!");
                    msg += "\n" + user.getTranslation(NEGATIVE, "For your security you were banned 10 seconds.");
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

    @Command(desc = "Shows the online mode")
    public void onlinemode(CommandContext context)
    {
        if (this.core.getServer().getOnlineMode())
        {
            context.sendTranslated(POSITIVE, "The Server is running in online mode");
            return;
        }
        context.sendTranslated(POSITIVE, "The Server is running in offline mode");
        /* Changing online mode is no longer supported on a running server
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
    public void loglevel(CommandContext context, @Optional @Label("loglevel") LogLevel level)
    {
        if (level != null)
        {
            context.getCore().getLog().setLevel(level);
            context.sendTranslated(POSITIVE, "New log level successfully set!");
            return;
        }
        context.sendTranslated(NEUTRAL, "The current log level is: {input#loglevel}", context.getCore().getLog().getLevel().getName());
    }

    @Command(alias = "finduser", desc = "Searches for a user in the database")
    public void searchuser(CommandContext context,
        @Reader(FindUserReader.class)
        @Label("name")
        @Desc("The name to search for")
        User user)
    {
        if (user.getName().equalsIgnoreCase(context.getString(0)))
        {
            context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", user);
            return;
        }
        context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", user);
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
                throw new ReaderException("No match found for {input}!", name);
            }
            return found;
        }
    }
}
