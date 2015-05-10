package de.cubeisland.engine.module.core.sponge.command;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import de.cubeisland.engine.butler.filter.Restricted;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Default;
import de.cubeisland.engine.butler.parametric.Desc;
import de.cubeisland.engine.butler.parametric.Optional;
import de.cubeisland.engine.module.core.ban.BanManager;
import de.cubeisland.engine.module.core.ban.IpBan;
import de.cubeisland.engine.module.core.ban.UserBan;
import de.cubeisland.engine.module.core.command.CommandContext;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.annotation.CommandPermission;
import de.cubeisland.engine.module.core.command.annotation.Unloggable;
import de.cubeisland.engine.module.core.permission.PermDefault;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.user.UserList;
import de.cubeisland.engine.module.core.user.UserManager;
import org.spongepowered.api.text.Texts;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.*;

public class AuthCommands
{
    private final SpongeCore core;
    private final BanManager banManager;
    private final UserManager um;

    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();

    public AuthCommands(SpongeCore core)
    {
        this.core = core;
        this.banManager = core.getBanManager();
        this.um = core.getUserManager();
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
}
