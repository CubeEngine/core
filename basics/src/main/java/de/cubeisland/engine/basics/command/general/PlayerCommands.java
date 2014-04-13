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
package de.cubeisland.engine.basics.command.general;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.TimeUtil;
import de.cubeisland.engine.core.util.math.BlockVector3;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.text.DateFormat.SHORT;

public class PlayerCommands
{
    private final UserManager um;
    private final Basics module;
    private AfkListener afkListener;

    public PlayerCommands(Basics basics)
    {
        this.module = basics;
        this.um = basics.getCore().getUserManager();
        final long autoAfk;
        final long afkCheck;
        afkCheck = basics.getConfiguration().autoAfk.check.getMillis();
        if (afkCheck > 0)
        {
            autoAfk = basics.getConfiguration().autoAfk.after.getMillis();
            this.afkListener = new AfkListener(basics, autoAfk, afkCheck);
            basics.getCore().getEventManager().registerListener(basics, this.afkListener);
            if (autoAfk > 0)
            {
                basics.getCore().getTaskManager().runTimer(basics, this.afkListener, 20, afkCheck / 50); // this is in ticks so /50
            }
        }
    }

    @Command(desc = "Refills your hunger bar", max = 1, usage = "(players)")
    public void feed(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_FEED_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to feed other players!");
                return;
            }
            Collection<User> users;
            boolean all = false;
            if (context.getString(0).equals("*"))
            {
                all = true;
                users = this.um.getOnlineUsers();
                if (users.isEmpty())
                {
                    context.sendTranslated(NEGATIVE, "There are no players online at the moment!");
                    return;
                }
                context.sendTranslated(POSITIVE, "You made everyone fat!");
                this.um.broadcastStatus(ChatFormat.BRIGHT_GREEN + "shared food with everyone.", context.getSender()); // TODO MessageType separate for translate Messages and messages from external input e.g. /me
            }
            else
            {
                users = new ArrayList<>();
                String[] userNames = StringUtils.explode(",",context.getString(0));
                for (String name : userNames)
                {
                    User user = this.um.findUser(name);
                    if (user == null || !user.isOnline())
                    {
                        context.sendTranslated(NEGATIVE, "Player {user} not found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated(NEUTRAL, "Could not find any of those players to feed!");
                    return;
                }
                context.sendTranslated(POSITIVE, "Fed {amount} players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated(POSITIVE, "You got fed by {user}!", context.getSender());
                }
                user.setFoodLevel(20);
                user.setSaturation(20);
                user.setExhaustion(0);
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.setFoodLevel(20);
            sender.setSaturation(20);
            sender.setExhaustion(0);
            context.sendTranslated(POSITIVE, "You are now fed!");
            return;
        }
        context.sendTranslated(NEGATIVE, "Don't feed the troll!");
        context.sendMessage(context.getCommand().getUsage(context));
    }

    @Command(desc = "Empties the hunger bar", max = 1, usage = "(players)")
    public void starve(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_STARVE_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to let other players starve!");
                return;
            }
            Collection<User> users;
            boolean all = false;
            if (context.getString(0).equals("*"))
            {
                all = true;
                users = this.um.getOnlineUsers();
                if (users.isEmpty())
                {
                    context.sendTranslated(NEGATIVE, "There are no players online at the moment!");
                    return;
                }
                context.sendTranslated(NEUTRAL, "You let everyone starve to death!");
                this.um.broadcastStatus(ChatFormat.YELLOW + "took away all food.", context.getSender());
            }
            else
            {
                users = new ArrayList<>();
                String[] userNames = StringUtils.explode(",",context.getString(0));
                for (String name : userNames)
                {
                    User user = this.um.findUser(name);
                    if (user == null || !user.isOnline())
                    {
                        context.sendTranslated(NEGATIVE, "Player {user} not found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated(NEUTRAL, "Could not find any of those players to starve!");
                    return;
                }
                context.sendTranslated(POSITIVE, "Starved {amount} players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated(NEUTRAL, "You are suddenly starving!");
                }
                user.setFoodLevel(0);
                user.setSaturation(0);
                user.setExhaustion(4);
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.setFoodLevel(0);
            sender.setSaturation(0);
            sender.setExhaustion(4);
            context.sendTranslated(NEGATIVE, "You are now starving!");
            return;
        }
        context.sendTranslated(NEGATIVE, "\n\n\n\n\n\n\n\n\n\n\n\n\nI'll give you only one line to eat!");
        context.sendMessage(context.getCommand().getUsage(context));
    }

    @Command(desc = "Heals a player", max = 1, usage = "(player)")
    public void heal(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_HEAL_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to heal other players!");
                return;
            }
            Collection<User> users;
            boolean all = false;
            if (context.getString(0).equals("*"))
            {
                all = true;
                users = this.um.getOnlineUsers();
                if (users.isEmpty())
                {
                    context.sendTranslated(NEGATIVE, "There are no players online at the moment!");
                    return;
                }
                context.sendTranslated(POSITIVE, "You healed everyone!");
                this.um.broadcastStatus(ChatFormat.BRIGHT_GREEN + "healed every player.", context.getSender());
            }
            else
            {
                users = new ArrayList<>();
                String[] userNames = StringUtils.explode(",",context.getString(0));
                for (String name : userNames)
                {
                    User user = this.um.findUser(name);
                    if (user == null || !user.isOnline())
                    {
                        context.sendTranslated(NEGATIVE, "Player {user} not found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated(NEUTRAL, "Could not find any of those players to heal!");
                    return;
                }
                context.sendTranslated(POSITIVE, "Healed {amount} players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated(POSITIVE, "You got healed by {sender}!", context.getSender().getName());
                }
                user.setHealth(user.getMaxHealth());
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.setHealth(sender.getMaxHealth());
            sender.sendTranslated(POSITIVE, "You are now healed!");
            return;
        }
        context.sendTranslated(NEGATIVE, "Only time can heal your wounds!");
        context.sendMessage(context.getCommand().getUsage(context));
    }

    private GameMode getGameMode(String name)
    {
        if (name == null)
        {
            return null;
        }
        switch (name.trim().toLowerCase())
        {
            case "survival":
            case "s":
            case "0":
                return GameMode.SURVIVAL;
            case "creative":
            case "c":
            case "1":
                return GameMode.CREATIVE;
            case "adventure":
            case "a":
            case "2":
                return GameMode.ADVENTURE;
            default:
                return null;
        }
    }

    private GameMode toggleGameMode(GameMode mode)
    {
        switch (mode)
        {
            case SURVIVAL:
                return GameMode.CREATIVE;
            case ADVENTURE:
            case CREATIVE:
            default:
                return GameMode.SURVIVAL;
        }
    }

    @Command(names = {"gamemode", "gm"}, max = 2,
            desc = "Changes the gamemode", usage = "(player) [gamemode]")
    public void gamemode(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = null;
        if (context.getArgCount() > 0)
        {
            if (context.getArgCount() > 1 || getGameMode(context.getString(0)) == null)
            {
                target = um.findUser(context.getString(0));
                if (target == null)
                {
                    context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(0));
                    return;
                }
            }
        }
        if (target == null)
        {
            if (sender instanceof User)
            {
                target = (User)sender;
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You do not not have any game mode!");
                return;
            }
        }
        if (sender != target && !module.perms().COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to change the game mode of an other player!");
            return;
        }
        GameMode newMode = getGameMode(context.getString(context.getArgCount() - 1));
        if (newMode == null)
        {
            newMode = toggleGameMode(target.getGameMode());
        }
        target.setGameMode(newMode);
        if (sender != target)
        {
            context.sendTranslated(POSITIVE, "You changed the game mode of {user} to {input#gamemode}!", target.getDisplayName(), newMode.name()); // TODO translate gamemode
            target.sendTranslated(NEUTRAL, "Your game mode has been changed to {input#gamemode}!", newMode.name());
        }
        else
        {
            context.sendTranslated(POSITIVE, "You changed your game mode to {input#gamemode}!", newMode.name());
        }
    }

    @Command(names = {
        "kill", "slay"
    }, desc = "Kills a player", usage = "<player>", flags = {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "quiet", name = "q"),
        @Flag(longName = "lightning", name = "l")
    }, min = 0 , max = 1)
    public void kill(ParameterizedContext context)
    {
        boolean lightning = context.hasFlag("l") && module.perms().COMMAND_KILL_LIGHTNING.isAuthorized(context.getSender());
        boolean force = context.hasFlag("f") && module.perms().COMMAND_KILL_FORCE.isAuthorized(context.getSender());
        boolean quiet = context.hasFlag("q") && module.perms().COMMAND_KILL_QUIET.isAuthorized(context.getSender());
        if (context.hasArg(0))
        {
            String[] names = StringUtils.explode(",",context.getString(0));
            List<String> killed = new ArrayList<>();
            if ("*".equals(names[0]))
            {
                if (!module.perms().COMMAND_KILL_ALL.isAuthorized(context.getSender()))
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to kill everyone!");
                    return;
                }
                for (User user : context.getCore().getUserManager().getOnlineUsers())
                {
                    if (!user.equals(context.getSender()))
                    {
                        if (this.kill(user, lightning, context, false, force, quiet))
                        {
                            killed.add(user.getDisplayName());
                        }
                    }
                }
            }
            else
            {
                for (String name : names)
                {
                    User user = this.um.findUser(name);
                    if (user == null || !user.isOnline())
                    {
                        context.sendTranslated(NEGATIVE, "Player {user} not found or offline!", name);
                        continue;
                    }
                    if (this.kill(user, lightning, context, false, force, quiet))
                    {
                        killed.add(user.getDisplayName());
                    }
                }
            }

            if (killed.isEmpty())
            {
                if (names.length == 1)
                {
                    context.sendTranslated(NEGATIVE, "Could not kill {user}", names[0]);
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "Could not kill any of given players!");
                }
                return;
            }
            context.sendTranslated(POSITIVE, "You killed {user#list}!", StringUtils.implode(",", killed));
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            TreeSet<Entity> entities = sender.getTargets(150);
            User user = null;
            for (Entity entity : entities)
            {
                if (!sender.hasLineOfSight(entity))
                {
                    break; // entity cannot be seen directly
                }
                if (entity instanceof Player)
                {
                    user = this.um.getExactUser(entity.getUniqueId());
                    break;
                }
            }
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "No player to kill in sight!");
                return;
            }
            this.kill(user,lightning,context,true,force, quiet);
            return;
        }
        context.sendTranslated(NEGATIVE, "Please specify a victim!");
    }

    private boolean kill(User user, boolean lightning, ParameterizedContext context, boolean showMessage, boolean force, boolean quiet)
    {
        if (!force)
        {
            if (module.perms().COMMAND_KILL_PREVENT.isAuthorized(user) || this.module.getBasicsUser(user).getbUEntity().getGodmode())
            {
                context.sendTranslated(NEGATIVE, "You cannot kill {user}!", user);
                return false;
            }
        }
        if (lightning)
        {
            user.getWorld().strikeLightningEffect(user.getLocation());
        }
        user.setHealth(0);
        if (showMessage)
        {
            context.sendTranslated(POSITIVE, "You killed {user}!", user);
        }
        if (!quiet && module.perms().COMMAND_KILL_NOTIFY.isAuthorized(user))
        {
            user.sendTranslated(NEUTRAL, "You were killed by {user}", context.getSender());
        }
        return true;
    }

    @Command(desc = "Shows when given player was online the last time", min = 1, max = 1, usage = "<player>")
    public void seen(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        if (user.isOnline())
        {
            context.sendTranslated(NEUTRAL, "{user} is currently online!", user);
            return;
        }
        long lastPlayed = user.getLastPlayed();
        if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) // If greater than 7 days show distance not date
        {
            Date date = new Date(lastPlayed);
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, context.getSender().getLocale());
            context.sendTranslated(NEUTRAL, "{user} is offline since {input#time}", user, format.format(date));
            return;
        }
        context.sendTranslated(NEUTRAL, "{user} was last seen {input#date}.", user, TimeUtil.format(context.getSender().getLocale(), new Date(lastPlayed)));
    }

    @Command(desc = "Makes a player send a message (including commands)",
             usage = "<player> <message>",
             min = 2, max = NO_MAX)
    public void sudo(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        String s = context.getStrings(1);
        if (s.startsWith("/"))
        {
            this.module.getCore().getCommandManager().runCommand(user,s.substring(1));
            context.sendTranslated(POSITIVE, "Command {input#command} executed as {user}", s, user);
            return;
        }
        user.chat(s);
        context.sendTranslated(POSITIVE, "Forced {user} to chat: {input#message}", user, s);
    }

    @Command(desc = "Kills yourself", max = 0)
    public void suicide(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "You want to kill yourself? {text:The command for that is stop!:color=BRIGHT_GREEN}");
            return;
        }
        User sender = (User)context.getSender();
        sender.setHealth(0);
        sender.setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, sender.getMaxHealth()));
        context.sendTranslated(NEGATIVE, "You ended your life. Why? {text:\\:(:color=DARK_RED}");
    }

    @Command(desc = "Displays that you are afk", max = 1, usage = "(player)")
    public void afk(CommandContext context)
    {
        User user;
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_AFK_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to change the afk-state of an other player!");
                return;
            }
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
            if (!user.isOnline())
            {
                context.sendTranslated(NEGATIVE, "{user} is not online!", user);
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Just go!");
            return;
        }
        if (!user.get(BasicsAttachment.class).isAfk())
        {
            user.get(BasicsAttachment.class).setAfk(true);
            user.get(BasicsAttachment.class).resetLastAction();
            this.um.broadcastStatus("is now afk.", user);
        }
        else
        {
            user.get(BasicsAttachment.class).updateLastAction();
            this.afkListener.run();
        }
    }

    @Command(desc = "Displays informations from a player!", usage = "<player>", min = 1, max = 1)
    public void whois(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        if (!user.isOnline())
        {
            context.sendTranslated(NEUTRAL, "Nickname: {user} ({text:offline})", user);
        }
        else
        {
            context.sendTranslated(NEUTRAL, "Nickname: {user}", user);
        }
        if (user.hasPlayedBefore() || user.isOnline())
        {
            context.sendTranslated(NEUTRAL, "Life: {decimal:0}/{decimal#max:0}", user.getHealth(), user.getMaxHealth());
            context.sendTranslated(NEUTRAL, "Hunger: {integer#foodlvl:0}/{text:20} ({integer#saturation}/{integer#foodlvl:0})", user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel());
            context.sendTranslated(NEUTRAL, "Level: {integer#level} + {integer#percent}%", user.getLevel(), (int)(user.getExp() * 100));
            Location loc = user.getLocation();
            if (loc != null)
            {
                context.sendTranslated(NEUTRAL, "Position: {vector} in {world}", new BlockVector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), loc.getWorld());
            }
            if (user.getAddress() != null)
            {
                context.sendTranslated(NEUTRAL, "IP: {input#ip}", user.getAddress().getAddress().getHostAddress());
            }
            if (user.getGameMode() != null)
            {
                context.sendTranslated(NEUTRAL, "Gamemode: {input#gamemode}", user.getGameMode().toString());
            }
            if (user.getAllowFlight())
            {
                context.sendTranslated(NEUTRAL, "Flymode: {text:true:color=BRIGHT_GREEN} {input#flying}", user.isFlying() ? "flying" : "not flying");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "Flymode: {text:false:color=RED}");
            }
            if (user.isOp())
            {
                context.sendTranslated(NEUTRAL, "OP: {text:true:color=BRIGHT_GREEN}");
            }
            Timestamp muted = module.getBasicsUser(user).getbUEntity().getMuted();
            if (muted != null && muted.getTime() > System.currentTimeMillis())
            {
                context.sendTranslated(NEUTRAL, "Muted until {input#time}", DateFormat.getDateTimeInstance(SHORT, SHORT, context.getSender().getLocale()).format(muted));
            }
            if (user.getGameMode() != GameMode.CREATIVE)
            {
                context.sendTranslated(NEUTRAL, "GodMode: {input#godmode}", user.isInvulnerable() ? ChatFormat.BRIGHT_GREEN + "true" : ChatFormat.RED + "false");
            }
            if (user.get(BasicsAttachment.class).isAfk())
            {
                context.sendTranslated(NEUTRAL, "AFK: {text:true:color=BRIGHT_GREEN}");
            }
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SHORT, SHORT, Locale.ENGLISH);
            context.sendTranslated(NEUTRAL, "First played: {input#date}", dateFormat.format(new Date(user.getFirstPlayed())));
        }
        if (this.module.getCore().getBanManager().isUserBanned(user.getUniqueId()))
        {
            UserBan ban = this.module.getCore().getBanManager().getUserBan(user.getUniqueId());
            String expires;
            DateFormat format = DateFormat.getDateTimeInstance(SHORT, SHORT, context.getSender().getLocale());
            if (ban.getExpires() != null)
            {
                expires = format.format(ban.getExpires());
            }
            else
            {
                expires = context.getSender().getTranslation(NONE, "for ever");
            }
            context.sendTranslated(NEUTRAL, "Banned by {user} on {input#date}: {input#reason} ({input#expire})", ban.getSource(), format.format(ban.getCreated()), ban.getReason(), expires);
        }
    }

    @Command(desc = "Toggles the god-mode!", usage = "[player]", max = 1)
    public void god(CommandContext context)
    {
        User user;
        boolean other = false;
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_GOD_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to god others!");
                return;
            }
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
            other = true;
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated(POSITIVE, "You are god already!");
            return;
        }
        BasicsUserEntity bUser = module.getBasicsUser(user).getbUEntity();
        bUser.setGodmode(!bUser.getGodmode());
        if (bUser.getGodmode())
        {
            if (other)
            {
                user.sendTranslated(POSITIVE, "You are now invincible!");
                context.sendTranslated(POSITIVE, "{user} is now invincible!", user);
                return;
            }
            context.sendTranslated(POSITIVE, "You are now invincible!");
            return;
        }
        if (other)
        {
            user.sendTranslated(NEUTRAL, "You are no longer invincible!");
            context.sendTranslated(NEUTRAL, "{user} is no longer invincible!", user);
            return;
        }
        context.sendTranslated(NEUTRAL, "You are no longer invincible!");
    }

    @Command(desc = "Changes your walkspeed.", usage = "<speed> [player]", min = 1, max = 2)
    public void walkspeed(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        User user = sender;
        boolean other = false;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user != sender)
            {
                other = true;
            }
        }
        else if (sender == null) // Sender is console and no player given!
        {
            context.sendTranslated(NEUTRAL, "You suddenly feel much faster!");
            return;
        }
        if (user == null || !user.isOnline())
        {
            context.sendTranslated(NEGATIVE, "User {user} not found or offline!", context.getString("player"));
            return;
        }
        if (other && !module.perms().COMMAND_WALKSPEED_OTHER.isAuthorized(context.getSender())) // PermissionChecks
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to change the walk speed of other user!");
            return;
        }
        user.setWalkSpeed(0.2f);
        Float speed = context.getArg(0, Float.class, null);
        if (speed != null && speed >= 0 && speed <= 10)
        {
            user.setWalkSpeed(speed / 10f);
            user.sendTranslated(POSITIVE, "You can now walk at {decimal:2}!", speed);
            return;
        }
        if (speed != null && speed > 9000)
        {
            user.sendTranslated(NEGATIVE, "It's over 9000!");
        }
        user.sendTranslated(NEUTRAL, "Walk speed has to be a Number between {text:0} and {text:10}!");
    }

    @Command(desc = "Lets you fly away", max = 2,
            params = @Param(names = { "player", "p"}, type = User.class),
            usage = "[flyspeed] [player]")
    public void fly(ParameterizedContext context)
    {
        final CommandSender sender = context.getSender();
        User target;
        if (context.hasArg(1))
        {
            target = context.getUser(1);
            if (target == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                target = (User) context.getSender();
            }
            else
            {
                context.sendTranslated(NEUTRAL, "{text:ProTip}: If your server flies away it will go offline.");
                context.sendTranslated(NEUTRAL, "So... Stopping the Server in {text:3..:color=RED}");
                return;
            }
        }
        // PermissionChecks
        if (sender != target && !module.perms().COMMAND_FLY_OTHER.isAuthorized(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to change the fly mode of other player!");
            return;
        }
        //I Believe I Can Fly ...
        if (context.hasArg(0))
        {
            Float speed = context.getArg(0, Float.class);
            if (speed != null && speed >= 0 && speed <= 10)
            {
                target.setFlySpeed(speed / 10f);
                context.sendTranslated(POSITIVE, "You can now fly at {decimal#speed:2}!", speed);
            }
            else
            {
                if (speed != null && speed > 9000)
                {
                    context.sendTranslated(NEUTRAL, "It's over 9000!");
                }
                context.sendTranslated(NEGATIVE, "FlySpeed has to be a Number between {text:0} and {text:10}!");
            }
            target.setAllowFlight(true);
            target.setFlying(true);
            return;
        }
        target.setAllowFlight(!target.getAllowFlight());
        if (target.getAllowFlight())
        {
            target.setFlySpeed(0.1f);
            context.sendTranslated(POSITIVE, "You can now fly!");
            return;
        }
        context.sendTranslated(NEUTRAL, "You cannot fly anymore!");
    }
}
