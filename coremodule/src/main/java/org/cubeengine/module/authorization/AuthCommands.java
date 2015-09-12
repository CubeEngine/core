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
package org.cubeengine.module.authorization;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import de.cubeisland.engine.butler.filter.Restricted;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Default;
import de.cubeisland.engine.butler.parametric.Desc;
import de.cubeisland.engine.butler.parametric.Optional;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.command.annotation.CommandPermission;
import org.cubeengine.service.command.annotation.Unloggable;
import org.cubeengine.service.user.MultilingualPlayer;
import org.cubeengine.service.user.UserList;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.ban.Bans;
import org.spongepowered.api.util.command.CommandSource;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cubeengine.service.i18n.formatter.MessageType.*;

public class AuthCommands
{
    private final Authorization module;
    private final Game game;
    private final BanService bs;

    private final ConcurrentHashMap<UUID, Long> fails = new ConcurrentHashMap<>();

    public AuthCommands(Authorization module, Game game, BanService bs)
    {
        this.module = module;
        this.game = game;
        this.bs = bs;
    }

    @Unloggable
    @Command(alias = "setpw", desc = "Sets your password.")
    public void setPassword(CommandContext context, String password, @Default MultilingualPlayer player)
    {
        if ((context.getSource().equals(player)))
        {
            module.getManager().setPassword(player.getUniqueId(), password);
            context.sendTranslated(POSITIVE, "Your password has been set!");
            return;
        }
        context.ensurePermission(module.perms().COMMAND_SETPASSWORD_OTHER);
        module.getManager().setPassword(player.getUniqueId(), password);
        context.sendTranslated(POSITIVE, "{user}'s password has been set!", player);
    }

    @Command(alias = "clearpw", desc = "Clears your password.")
    public void clearPassword(CommandContext context,
                              @Optional @Desc("* or a list of Players delimited by ,") UserList players)
    {
        CommandSource sender = context.getSource();
        if (players == null)
        {
            if (!(sender instanceof Player))
            {
                throw new TooFewArgumentsException();
            }
            module.getManager().resetPassword(((Player)sender).getUniqueId());
            context.sendTranslated(POSITIVE, "Your password has been reset!");
            return;
        }
        if (players.isAll())
        {
            context.ensurePermission(module.perms().COMMAND_CLEARPASSWORD_ALL);
            module.resetAllPasswords();
            context.sendTranslated(POSITIVE, "All passwords reset!");
            return;
        }
        context.ensurePermission(module.perms().COMMAND_CLEARPASSWORD_OTHER);
        for (Player user : players.list())
        {
            module.getManager().resetPassword(user.getUniqueId());
            context.sendTranslated(POSITIVE, "{user}'s password has been reset!", user.getName());
        }
    }

    @Unloggable
    @Command(desc = "Logs you in with your password!")
    @CommandPermission(checkPermission = false) // TODO assign by default
    @Restricted(value = MultilingualPlayer.class, msg = "Only players can log in!")
    public void login(MultilingualPlayer context, String password)
    {
        if (module.getManager().isLoggedIn(context.getUniqueId()))
        {
            context.sendTranslated(POSITIVE, "You are already logged in!");
            return;
        }
        boolean isLoggedIn = module.getManager().login(context.getUniqueId(), password);
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
                if (fails.get(context.getUniqueId()) + SECONDS.toMillis(10) > currentTimeMillis())
                {
                    Literal msg = Texts.of(context.getTranslation(NEGATIVE, "Too many wrong passwords!") + "\n"
                                    + context.getTranslation(NEUTRAL, "For your security you were banned 10 seconds."));
                    Date expires = new Date(currentTimeMillis() + SECONDS.toMillis(config.banDuration));
                    this.bs.ban(Bans.builder().user(context.original()).reason(msg).expirationDate(expires).source(context.original()).build());
                    if (!game.getServer().getOnlineMode())
                    {
                        this.bs.ban(Bans.builder().address(context.original().getConnection().getAddress().getAddress()).reason(msg).expirationDate(expires).source(context.original()).build());
                    }
                    context.original().kick(msg);
                }
            }
            fails.put(context.getUniqueId(), currentTimeMillis());
        }
    }

    @Command(desc = "Logs you out!")
    @Restricted(value = MultilingualPlayer.class, msg = "You might use /stop for this.")
    public void logout(MultilingualPlayer context)
    {
        if (module.getManager().isLoggedIn(context.getUniqueId()))
        {
            module.getManager().logout(context.getUniqueId());
            context.sendTranslated(POSITIVE, "You're now logged out.");
            return;
        }
        context.sendTranslated(NEUTRAL, "You're not logged in!");
    }
}
