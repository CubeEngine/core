package de.cubeisland.engine.module.core;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.filter.Restricted;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Default;
import de.cubeisland.engine.butler.parametric.Desc;
import de.cubeisland.engine.butler.parametric.Flag;
import de.cubeisland.engine.butler.parametric.Optional;
import de.cubeisland.engine.butler.parametric.Reader;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.module.core.ban.BanManager;
import de.cubeisland.engine.module.core.ban.IpBan;
import de.cubeisland.engine.module.core.ban.UserBan;
import de.cubeisland.engine.module.core.command.CommandContext;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.ContainerCommand;
import de.cubeisland.engine.module.core.command.annotation.CommandPermission;
import de.cubeisland.engine.module.core.command.annotation.Unloggable;
import de.cubeisland.engine.module.core.permission.PermDefault;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.user.UserList;
import de.cubeisland.engine.module.core.user.UserManager;
import de.cubeisland.engine.module.core.util.Profiler;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.text.Texts;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.*;

@Command(name = "cubeengine", alias = "ce",
    desc = "These are the basic commands of the CubeEngine.")
public class CoreCommands extends ContainerCommand
{
    private final SpongeCore core;
    private final BanManager banManager;
    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();
    private final UserManager um;

    public CoreCommands(SpongeCore core)
    {
        super(core);
        this.core = core;
        this.banManager = core.getBanManager();
        this.um = core.getUserManager();

        core.getCommandManager().getProviderManager().register(core,
                                                               new FindUserReader());
    }

    @Command(desc = "Reloads the whole CubeEngine")
    public void reload(CommandSender context)
    {
        context.sendTranslated(POSITIVE, "Reloading CubeEngine! This may take some time...");
        final long startTime = System.currentTimeMillis();

        PluginManager pm = core.getGame().getPluginManager();

        context.sendTranslated(POSITIVE, "CubeEngine Reload completed in {integer#time}ms!",
                               System.currentTimeMillis() - startTime);
    }

    @Command(desc = "Reloads all of the modules!")
    public void reloadmodules(CommandSender context, @Flag boolean file)
    {
        context.sendTranslated(POSITIVE, "Reloading all modules! This may take some time...");
        Profiler.startProfiling("modulesReload");
        core.getModulatiry().reloadModules(file);
        long time = Profiler.endProfiling("modulesReload", TimeUnit.SECONDS);
        context.sendTranslated(POSITIVE, "Modules Reload completed in {integer#time}s!", time);
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    public void setPassword(CommandContext context, String password, @Default User player)
    {
        if ((context.getSource().equals(player)))
        {
            um.setPassword(player, password);
            context.sendTranslated(POSITIVE, "Your password has been set!");
            return;
        }
        context.ensurePermission(core.perms().COMMAND_SETPASSWORD_OTHER);
        um.setPassword(player, password);
        context.sendTranslated(POSITIVE, "{user}'s password has been set!", player);
    }

    @Command(alias = "clearpw", desc = "Clears your password.")
    public void clearPassword(CommandContext context,
                              @Optional @Desc("* or a list of Players delimited by ,") UserList players)
    {
        CommandSender sender = context.getSource();
        if (players == null)
        {
            if (!(sender instanceof User))
            {
                throw new TooFewArgumentsException();
            }
            this.um.resetPassword((User)sender);
            sender.sendTranslated(POSITIVE, "Your password has been reset!");
            return;
        }
        if (players.isAll())
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
    @CommandPermission(permDefault = PermDefault.TRUE)
    @Restricted(value = User.class, msg = "Only players can log in!")
    public void login(User context, String password)
    {
        if (context.isLoggedIn())
        {
            context.sendTranslated(POSITIVE, "You are already logged in!");
            return;
        }
        boolean isLoggedIn = um.login(context, password);
        if (isLoggedIn)
        {
            context.sendTranslated(POSITIVE, "You logged in successfully!");
            return;
        }
        context.sendTranslated(NEGATIVE, "Wrong password!");
        if (this.core.getConfiguration().security.fail2ban)
        {
            if (fails.get(context.getUniqueId()) != null)
            {
                if (fails.get(context.getUniqueId()) + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis())
                {
                    String msg = context.getTranslation(NEGATIVE, "Too many wrong passwords!") + "\n"
                        + context.getTranslation(NEGATIVE, "For your security you were banned 10 seconds.");
                    this.banManager.addBan(new UserBan(context.getOfflinePlayer(), context.getPlayer().get(), Texts.of(
                        msg), new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
                        this.core.getConfiguration().security.banDuration))));

                    if (!core.getGame().getServer().getOnlineMode())
                    {
                        this.banManager.addBan(new IpBan(context.getAddress().getAddress(), context.getPlayer().get(),
                                                         Texts.of(msg), new Date(
                            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
                                this.core.getConfiguration().security.banDuration))));
                    }
                    context.kick(Texts.of(msg));
                }
            }
            fails.put(context.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Command(desc = "Logs you out!")
    @Restricted(value = User.class, msg = "You might use /stop for this.")
    public void logout(User context)
    {
        if (context.isLoggedIn())
        {
            context.logout();
            context.sendTranslated(POSITIVE, "You're now logged out.");
            return;
        }
        context.sendTranslated(NEUTRAL, "You're not logged in!");
    }

    @Command(desc = "Shows the online mode")
    public void onlinemode(CommandSender context)
    {
        if (this.core.getGame().getServer().getOnlineMode())
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
    public void loglevel(CommandSender context, @Optional LogLevel loglevel)
    {
        if (loglevel != null)
        {
            core.getLog().setLevel(loglevel);
            context.sendTranslated(POSITIVE, "New log level successfully set!");
            return;
        }
        context.sendTranslated(NEUTRAL, "The current log level is: {input#loglevel}",
                               core.getLog().getLevel().getName());
    }

    @Command(alias = "finduser", desc = "Searches for a user in the database")
    public void searchuser(CommandContext context,
                           @Reader(FindUserReader.class) @Desc("The name to search for") User name)
    {
        if (name.getName().equalsIgnoreCase(context.getString(0)))
        {
            context.sendTranslated(POSITIVE, "Matched exactly! User: {user}", name);
            return;
        }
        context.sendTranslated(POSITIVE, "Matched not exactly! User: {user}", name);
    }

    public static class FindUserReader implements ArgumentReader<User>
    {
        @Override
        public User read(Class type, CommandInvocation invocation) throws ReaderException
        {
            String name = invocation.consume(1);
            UserManager um = CubeEngine.getCore().getUserManager();
            User found = um.findExactUser(name);
            if (found == null)
            {
                found = um.findUser(name, true);
            }
            if (found == null)
            {
                throw new ReaderException("No match found for {input}!", name);
            }
            return found;
        }
    }
}
