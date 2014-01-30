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

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;
import static java.text.DateFormat.SHORT;

public class PlayerCommands
{
    private UserManager um;
    private Basics module;
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

    @Command(desc = "Refills your hunger bar", max = 1, usage = "{players}")
    public void feed(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_FEED_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to feed other users!");
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
                    context.sendTranslated("&cThere are no users online at the moment!");
                    return;
                }
                context.sendTranslated("&6You made everyone fat!");
                this.um.broadcastStatus("&ashared food with everyone.", context.getSender());
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
                        context.sendTranslated("&cUser &2%s &cnot found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated("&eCould not find any of those users to feed!");
                    return;
                }
                context.sendTranslated("&aFeeded &6%d&a players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated("&aYou got fed by &2%s&a!", context.getSender().getName());
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
            context.sendTranslated("&aYou are now fed!");
            return;
        }
        context.sendTranslated("&cDon't feed the troll!");
        context.sendMessage(context.getCommand().getUsage(context));
    }

    @Command(desc = "Empties the hunger bar", max = 1, usage = "{players}")
    public void starve(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_STARVE_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to let other user starve!");
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
                    context.sendTranslated("&cThere are no users online at the moment!");
                    return;
                }
                context.sendTranslated("&eYou let everyone starve to death!");
                this.um.broadcastStatus("&etook away all food.", context.getSender());
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
                        context.sendTranslated("&cUser &2%s &cnot found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated("&eCould not find any of those users to starve!");
                    return;
                }
                context.sendTranslated("&aStarved &6%d&a players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated("&eYou are suddenly starving!");
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
            context.sendTranslated("&6You are now starving!");
            return;
        }
        context.sendTranslated("\n\n\n\n\n\n\n\n\n\n\n\n\n&cI'll give you only one line to eat!");
        context.sendMessage(context.getCommand().getUsage(context));
    }

    @Command(desc = "Heals a Player", max = 1, usage = "{player}")
    public void heal(CommandContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_HEAL_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to heal other user!");
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
                    context.sendTranslated("&cThere are no users online at the moment!");
                    return;
                }
                context.sendTranslated("&aYou healed everyone!");
                this.um.broadcastStatus("&ahealed every player.", context.getSender());
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
                        context.sendTranslated("&cUser &2%s &cnot found!", name);
                        continue;
                    }
                    users.add(user);
                }
                if (users.isEmpty())
                {
                    context.sendTranslated("&eCould not find any of those users to heal!");
                    return;
                }
                context.sendTranslated("&aHealed &6%d&a players!", users.size());
            }
            for (User user : users)
            {
                if (!all)
                {
                    user.sendTranslated("&aYou got healed by &2%s&a!", context.getSender().getName());
                }
                user.setHealth(user.getMaxHealth());
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.setHealth(sender.getMaxHealth());
            sender.sendTranslated("&aYou are now healed!");
            return;
        }
        context.sendTranslated("&cOnly time can heal your wounds!");
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
            desc = "Changes the gamemode", usage = "{player} [gamemode]")
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
                    context.sendTranslated("&cCould not find a user for &2%s&c!", context.getString(0));
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
                context.sendTranslated("&cYou do not not have any game mode!");
                return;
            }
        }
        if (sender != target && !module.perms().COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            context.sendTranslated("&cYou are not allowed to change the game mode of an other player!");
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
            context.sendTranslated("&aYou changed the game mode of &2%s &ato &6%s&a!", target.getDisplayName(), sender.translate(newMode.toString()));
            target.sendTranslated("&eYour game mode has been changed to &6%s&a!", target.translate(newMode.toString()));
        }
        else
        {
            context.sendTranslated("&aYou changed your game mode to &6%s&a!", sender.translate(newMode.toString()));
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
                    context.sendTranslated("&cYou are not allowed to kill everyone!");
                    return;
                }
                for (User user : context.getCore().getUserManager().getOnlineUsers())
                {
                    if (!user.getName().equals(context.getSender().getName()))
                    {
                        if (this.kill(user, lightning, context, false, force, quiet))
                        {
                            killed.add(user.getName());
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
                        context.sendTranslated("&cUser %s not found or offline!", name);
                        continue;
                    }
                    if (this.kill(user, lightning, context, false, force, quiet))
                    {
                        killed.add(user.getName());
                    }
                }
            }

            if (killed.isEmpty())
            {
                if (names.length == 1)
                {
                    context.sendTranslated("&cCould not kill &2%s", names[0]);
                }
                else
                {
                    context.sendTranslated("&eCould not kill any of given users!");
                }
                return;
            }
            context.sendTranslated("&aYou killed &2%s&a!", StringUtils.implode(",", killed));
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
                    user = this.um.getExactUser(((Player)entity).getName());
                    break;
                }
            }
            if (user == null)
            {
                context.sendTranslated("&cNo player to kill in sight!");
                return;
            }
            this.kill(user,lightning,context,true,force, quiet);
            return;
        }
        context.sendTranslated("&cPlease specify a victim!");
    }

    private boolean kill(User user, boolean lightning, ParameterizedContext context, boolean showMessage, boolean force, boolean quiet)
    {
        if (!force)
        {
            if (module.perms().COMMAND_KILL_PREVENT.isAuthorized(user) || this.module.getBasicsUser(user).getbUEntity().getGodmode())
            {
                context.sendTranslated("&cYou cannot kill &2%s&c!", user.getDisplayName());
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
            context.sendTranslated("&aYou killed &2%s&a!", user.getDisplayName());
        }
        if (!quiet && module.perms().COMMAND_KILL_NOTIFY.isAuthorized(user))
        {
            user.sendTranslated("&eYou were killed by &2%s",context.getSender().getDisplayName());
        }
        return true;
    }

    @Command(desc = "Shows when given player was online the last time", min = 1, max = 1, usage = "<player>")
    public void seen(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        if (user.isOnline())
        {
            context.sendTranslated("&2%s &eis currently online!", user.getName());
            return;
        }
        long lastPlayed = user.getLastPlayed();
        if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) // If greater than 7 days show distance not date
        {
            Date date = new Date(lastPlayed);
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, user.getLocale());
            context.sendTranslated("&2%s&e is offline since %s", user.getName(), format.format(date));
            return;
        }
        context.sendTranslated("&2%s&e was last seen &6%s.", user.getName(),
                   TimeUtil.format(context.getSender().getLocale(), new Date(lastPlayed)));
    }

    @Command(desc = "Makes a player send a message (including commands)",
             usage = "<player> <message>",
             min = 2, max = NO_MAX)
    public void sudo(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        String s = context.getStrings(1);
        if (s.startsWith("/"))
        {
            this.module.getCore().getCommandManager().runCommand(user,s.substring(1));
            context.sendTranslated("&aCommand &6%s &aexecuted as &2%s", s, user.getName());
            return;
        }
        user.chat(s);
        context.sendTranslated("&aForced &2%s&a to chat: &6%s", user.getName(), s);
    }

    @Command(desc = "Kills yourself", max = 0)
    public void suicide(CommandContext context)
    {
        User sender = this.um.getExactUser(context.getSender());
        if (sender == null)
        {
            context.sendTranslated("&cYou want to kill yourself? &aThe command for that is stop!");
            return;
        }
        sender.setHealth(0);
        sender.setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, sender.getMaxHealth()));
        context.sendTranslated("&eYou ended your pitiful life. &cWhy? &4:(");
    }

    @Command(desc = "Displays that you are afk", max = 1, usage = "{player}")
    public void afk(CommandContext context)
    {
        User user;
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_AFK_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to change the afk-state of an other player!");
                return;
            }
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
            if (!user.isOnline())
            {
                context.sendTranslated("&2%s &cis not online!", user.getDisplayName());
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated("&cJust go!");
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
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
            return;
        }
        if (!user.isOnline())
        {
            context.sendTranslated("&eNickname: &2%s&e &f(&eoffline&f)", user.getName());
        }
        else
        {
            context.sendTranslated("&eNickname: &2%s", user.getName());
        }
        if (user.hasPlayedBefore() || user.isOnline())
        {
            context.sendTranslated("&eLife: &2%.0f&f/&2%.0f", user.getHealth(), user.getMaxHealth());
            context.sendTranslated("&eHunger: &2%d&f/&220 &f(&2%d&f/&2%d&f)", user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel());
            context.sendTranslated("&eLevel: &2%d &f+ &2%d%%", user.getLevel(), (int)(user.getExp() * 100));
            Location loc = user.getLocation();
            if (loc != null)
            {
                context.sendTranslated("&ePosition: &2%d&f:&2%d&f:&2%d&e in &6%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
            }
            if (user.getAddress() != null)
            {
                context.sendTranslated("&eIP: &2%s", user.getAddress().getAddress().getHostAddress());
            }
            if (user.getGameMode() != null)
            {
                context.sendTranslated("&eGamemode: &2%s", user.getGameMode().toString());
            }
            if (user.getAllowFlight())
            {
                context.sendTranslated("&eFlymode: &atrue &f(%s)", user.isFlying() ? "flying" : "not flying");
            }
            else
            {
                context.sendTranslated("&eFlymode: &cfalse");
            }
            if (user.isOp())
            {
                context.sendTranslated("&eOP: &atrue");
            }
            Timestamp muted = module.getBasicsUser(user).getbUEntity().getMuted();
            if (muted != null && muted.getTime() > System.currentTimeMillis())
            {
                context.sendTranslated("&eMuted until &6%s", DateFormat.getDateTimeInstance(SHORT, SHORT, context.getSender().getLocale()).format(muted));
            }
            if (user.getGameMode() != GameMode.CREATIVE)
            {
                context.sendTranslated("&eGodMode: &2%s", user.isInvulnerable() ? ChatFormat.BRIGHT_GREEN + "true" : ChatFormat.RED + "false");
            }
            if (user.get(BasicsAttachment.class).isAfk())
            {
                context.sendTranslated("&eAFK: &atrue");
            }
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SHORT, SHORT, Locale.ENGLISH);
            context.sendTranslated("&eFirst played: &6%s", dateFormat.format(new Date(user.getFirstPlayed())));
        }
        if (this.module.getCore().getBanManager().isUserBanned(user.getName()))
        {
            UserBan ban = this.module.getCore().getBanManager().getUserBan(user.getName());
            String expires;
            DateFormat format = DateFormat.getDateTimeInstance(SHORT, SHORT, context.getSender().getLocale());
            if (ban.getExpires() != null)
            {
                expires = format.format(ban.getExpires());
            }
            else
            {
                expires = context.getSender().translate("for ever");
            }
            context.sendTranslated("&eBanned by &2%s&e on &6%s&e: &6%s&e (&6%s&f)", ban.getSource(), format.format(ban.getCreated()), ban.getReason(), expires);
        }
    }

    @Command(desc = "Toggles the god-mode!", usage = "[player]", max = 1)
    public void god(CommandContext context)
    {
        User user = null;
        boolean other = false;
        if (context.hasArg(0))
        {
            if (!module.perms().COMMAND_GOD_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to god others!");
                return;
            }
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
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
            context.sendTranslated("&aYou are god already!");
            return;
        }
        BasicsUserEntity bUser = module.getBasicsUser(user).getbUEntity();
        bUser.setGodmode(!bUser.getGodmode());
        if (bUser.getGodmode())
        {
            if (other)
            {
                user.sendTranslated("&aYou are now invincible!");
                context.sendTranslated("&2%s&a is now invincible!", user.getName());
                return;
            }
            context.sendTranslated("&aYou are now invincible!");
            return;
        }
        if (other)
        {
            user.sendTranslated("&eYou are no longer invincible!");
            context.sendTranslated("&2%s&e is no longer invincible!", user.getName());
            return;
        }
        context.sendTranslated("&eYou are no longer invincible!");
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
            context.sendTranslated("&eYou suddenly feel much faster!");
            return;
        }
        if (user == null || !user.isOnline())
        {
            context.sendTranslated("&cUser %s not found or offline!", context.getString("player"));
            return;
        }
        if (other && !module.perms().COMMAND_WALKSPEED_OTHER.isAuthorized(context.getSender())) // PermissionChecks
        {
            context.sendTranslated("&cYou are not allowed to change the walk-speed of other user!");
            return;
        }
        user.setWalkSpeed(0.2f);
        Float speed = context.getArg(0, Float.class, null);
        if (speed != null && speed >= 0 && speed <= 10)
        {
            user.setWalkSpeed(speed / 10f);
            user.sendTranslated("&aYou can now walk at &6%.2f&a.", speed);
            return;
        }
        if (speed != null && speed > 9000)
        {
            user.sendTranslated("&cIt's over 9000!");
        }
        user.sendTranslated("&eWalkspeed has to be a Number between &60 &eand &610&e!");
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
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                target = (User) context.getSender();
            }
            else
            {
                context.sendTranslated("&6ProTip: &eIf your server flies away it will go offline.");
                context.sendTranslated("&eSo... Stopping the Server in &c3..");
                return;
            }
        }
        if (target == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!");
            return;
        }
        // PermissionChecks
        if (sender != target && !module.perms().COMMAND_FLY_OTHER.isAuthorized(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed to change the fly-mode of other user!");
            return;
        }
        //I Believe I Can Fly ...
        if (context.hasArg(0))
        {
            Float speed = context.getArg(0, Float.class);
            if (speed != null && speed >= 0 && speed <= 10)
            {
                target.setFlySpeed(speed / 10f);
                context.sendTranslated("&aYou can now fly at &6%.2f&a!", speed);
            }
            else
            {
                if (speed != null && speed > 9000)
                {
                    context.sendTranslated("&6It's over 9000!");
                }
                context.sendTranslated("&cFlySpeed has to be a Number between &60 &cand &610&c!");
            }
            target.setAllowFlight(true);
            target.setFlying(true);
            return;
        }
        target.setAllowFlight(!target.getAllowFlight());
        if (target.getAllowFlight())
        {
            target.setFlySpeed(0.1f);
            context.sendTranslated("&aYou can now fly!");
            return;
        }
        context.sendTranslated("&eYou cannot fly anymore!");
    }
}
