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
package de.cubeisland.engine.module.authorization;

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
import de.cubeisland.engine.service.ban.BanManager;
import de.cubeisland.engine.service.ban.IpBan;
import de.cubeisland.engine.service.ban.UserBan;
import de.cubeisland.engine.service.command.CommandContext;
import de.cubeisland.engine.service.command.CommandSender;
import de.cubeisland.engine.service.command.annotation.CommandPermission;
import de.cubeisland.engine.service.command.annotation.Unloggable;
import de.cubeisland.engine.service.permission.PermDefault;
import de.cubeisland.engine.service.user.User;
import de.cubeisland.engine.service.user.UserList;
import org.spongepowered.api.Game;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.text.Texts;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.*;

public class AuthCommands
{
    private final Authorization module;
    private final Game game;
    private final BanManager banManager;

    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();

    public AuthCommands(Authorization module, Game game, BanManager bm)
    {
        this.module = module;
        this.game = game;
        this.banManager = bm;
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    public void setPassword(CommandContext context, String password, @Default User player)
    {
        if ((context.getSource().equals(player)))
        {
            player.attachOrGet(AuthAttachment.class, module).setPassword(password);
            context.sendTranslated(POSITIVE, "Your password has been set!");
            return;
        }
        context.ensurePermission(module.perms().COMMAND_SETPASSWORD_OTHER);
        player.attachOrGet(AuthAttachment.class, module).setPassword(password);
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
            ((User)sender).attachOrGet(AuthAttachment.class, module).resetPassword();
            sender.sendTranslated(POSITIVE, "Your password has been reset!");
            return;
        }
        if (players.isAll())
        {
            context.ensurePermission(module.perms().COMMAND_CLEARPASSWORD_ALL);
            module.resetAllPasswords();
            sender.sendTranslated(POSITIVE, "All passwords reset!");
            return;
        }
        context.ensurePermission(module.perms().COMMAND_CLEARPASSWORD_OTHER);
        for (User user : players.list())
        {
            user.attachOrGet(AuthAttachment.class, module).resetPassword();
            sender.sendTranslated(POSITIVE, "{user}'s password has been reset!", user.getName());
        }
    }

    @Unloggable
    @Command(desc = "Logs you in with your password!")
    @CommandPermission(permDefault = PermDefault.TRUE)
    @Restricted(value = User.class, msg = "Only players can log in!")
    public void login(User context, String password)
    {
        AuthAttachment attachment = context.attachOrGet(AuthAttachment.class, module);
        if (attachment.isLoggedIn())
        {
            context.sendTranslated(POSITIVE, "You are already logged in!");
            return;
        }
        boolean isLoggedIn = attachment.login(password);
        if (isLoggedIn)
        {
            context.sendTranslated(POSITIVE, "You logged in successfully!");
            return;
        }
        context.sendTranslated(NEGATIVE, "Wrong password!");
        AuthConfiguration config = this.module.getConfig();
        if (config.fail2ban)
        {
            if (fails.get(context.getUniqueId()) != null)
            {
                if (fails.get(context.getUniqueId()) + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis())
                {
                    Literal msg = Texts.of(context.getTranslation(NEGATIVE, "Too many wrong passwords!") + "\n"
                                    + context.getTranslation(NEUTRAL, "For your security you were banned 10 seconds."));
                    this.banManager.addBan(new UserBan(context.getUser(), context, msg, new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
                        config.banDuration))));

                    if (!game.getServer().getOnlineMode())
                    {
                        this.banManager.addBan(new IpBan(context.getAddress().getAddress(), context, msg, new Date(
                            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
                                config.banDuration))));
                    }
                    context.asPlayer().kick(msg);
                }
            }
            fails.put(context.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Command(desc = "Logs you out!")
    @Restricted(value = User.class, msg = "You might use /stop for this.")
    public void logout(User context)
    {
        AuthAttachment attachment = context.attachOrGet(AuthAttachment.class, module);
        if (attachment.isLoggedIn())
        {
            attachment.logout();
            context.sendTranslated(POSITIVE, "You're now logged out.");
            return;
        }
        context.sendTranslated(NEUTRAL, "You're not logged in!");
    }
}
