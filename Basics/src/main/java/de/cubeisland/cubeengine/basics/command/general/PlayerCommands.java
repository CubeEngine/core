package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.sql.Timestamp;
import java.util.TreeSet;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class PlayerCommands
{
    private UserManager um;
    private Basics basics;

    public PlayerCommands(Basics basics)
    {
        this.basics = basics;
        this.um = basics.getUserManager();
    }

    @Command(desc = "Refills your hunger bar", max = 1, flags = @Flag(longName = "all", name = "a"), usage = "[player]|[-a]")
    public void feed(ParameterizedContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            context.sendMessage("basics", "&6You made everyone fat!");
            this.um.broadcastStatus("basics", "&ashared food with everyone.", context.getSender().getName());
        }
        else
        {
            User sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            User user = sender;
            boolean other = false;
            if (context.hasArg(0))
            {
                user = context.getUser(0);
                if (user == null)
                {
                    paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    blockCommand(context, "basics", "&cDon't feed the troll!");
                }
            }
            user.setFoodLevel(20);
            user.setSaturation(20);
            user.setExhaustion(0);
            if (other)
            {
                context.sendMessage("basics", "&aFeeded &2%s&a!", user.getName());
                user.sendMessage("basics", "&aYou got fed by &2%s&a!", context.getSender().getName());
            }
            else
            {
                context.sendMessage("basics", "&aYou are now fed!");
            }
        }
    }

    @Command(desc = "Empties the hunger bar", max = 1, flags = @Flag(longName = "all", name = "a"), usage = "[player]|[-a]")
    public void starve(ParameterizedContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.setFoodLevel(0);
                player.setSaturation(0);
                player.setExhaustion(4);
            }
            context.sendMessage("basics", "&eYou starve everyone to death!");
            this.um.broadcastStatus("basics", "&etook away all food.", context.getSender().getName());
        }
        else
        {
            User sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            User user = sender;
            boolean other = false;
            if (context.hasArg(0))
            {
                user = context.getUser(0);
                if (user == null)
                {
                    paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "\n\n\n\n\n\n\n\n\n\n\n\n\n&cI'll give you only one line to eat!");
                }
            }
            user.setFoodLevel(0);
            user.setSaturation(0);
            user.setExhaustion(4);
            if (other)
            {
                context.sendMessage("basics", "&eStarved &2%s&e!", user.getName());
                user.sendMessage("basics", "&eYou are suddenly starving!");
            }
            else
            {
                context.sendMessage("basics", "&6You are now starving!");
            }
        }
    }

    @Command(desc = "Heals a Player", max = 1, flags = @Flag(longName = "all", name = "a"), usage = "[player]|[-a]")
    public void heal(ParameterizedContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            context.sendMessage("basics", "&aYou healed everyone!");
            this.um.broadcastStatus("basics", "&ahealed every player.", context.getSender().getName());
        }
        else
        {
            User sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            User user = sender;
            boolean other = false;
            if (context.hasArg(0))
            {
                user = context.getUser(0);
                if (user == null)
                {
                    paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "&cOnly time can heal your wounds!");
                }
            }
            user.setHealth(user.getMaxHealth());
            user.setFoodLevel(20);
            user.setSaturation(20);
            user.setExhaustion(0);
            if (other)
            {
                context.sendMessage("basics", "&aHealed &2%s&a!", user.getName());
                user.sendMessage("basics", "&aYou got healed by &2%s&a!", context.getSender().getName());
            }
            else
            {
                context.sendMessage("basics", "&aYou are now healed!");
            }
        }
    }

    @Command(names = {
        "gamemode", "gm"
    }, max = 2, desc = "Changes the gamemode", usage = "[gamemode] [player]")
    public void gamemode(CommandContext context)
    {
        boolean changeOther = false;
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        User user = sender;
        if (user == null)
        {
            invalidUsage(context, "basics", "&cYou do not not have any gamemode!");
        }
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                paramNotFound(context, "core", "&cUser %s not found!", context.getString(1));
            }
            changeOther = true;
        }
        if (!BasicsPerm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            denyAccess(context, "basics", "&cYou are not allowed to change the gamemode of an other player!");
        }
        if (context.hasArg(0))
        {
            String mode = context.getString(0);
            if (mode.equals("survival") || mode.equals("s"))
            {
                user.setGameMode(GameMode.SURVIVAL);
            }
            else if (mode.equals("creative") || mode.equals("c"))
            {
                user.setGameMode(GameMode.CREATIVE);
            }
            else if (mode.equals("adventure") || mode.equals("a"))
            {
                user.setGameMode(GameMode.ADVENTURE);
            }
        }
        else
        {
            GameMode gamemode = user.getGameMode();
            switch (gamemode)
            {
                case ADVENTURE:
                case CREATIVE:
                    user.setGameMode(GameMode.SURVIVAL);
                    break;
                case SURVIVAL:
                    user.setGameMode(GameMode.CREATIVE);
            }
        }
        if (changeOther)
        {
            context.sendMessage("basics", "&aYou changed the gamemode of &2%s &ato &6%s&a!",
                    user.getName(), _(sender, "basics", user.getGameMode().toString()));
            user.sendMessage("basics", "&eYour Gamemode has been changed to &6%s&a!",
                    _(user, "basics", user.getGameMode().toString()));
        }
        else
        {
            context.sendMessage("basics", "&aYou changed your gamemode to &6%s&a!",
                    _(user, "basics", user.getGameMode().toString()));
        }
    }

    @Command(names = {
        "kill", "slay"
    }, desc = "Kills a player", usage = "<player>|-a", flags = {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "lightning", name = "l")
    })
    public void kill(ParameterizedContext context)
    {
        boolean lightning = context.hasFlag("l") && BasicsPerm.COMMAND_KILL_LIGHTNING.isAuthorized(context.getSender());
        boolean force = context.hasFlag("f") && BasicsPerm.COMMAND_KILL_FORCE.isAuthorized(context.getSender());
        User user = null;
        if (context.hasArg(0))
        {
            user = context.getUser(0);
        }
        else if (!context.hasFlag("a"))
        {
            User sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            if (sender == null)
            {
                context.sendMessage("basics", "&cPlease speicify a victim!");
                return;
            }
            TreeSet<Entity> entities = sender.getTargets(150);
            for (Entity entity : entities)
            {
                if (!sender.hasLineOfSight(entity))
                {
                    break; // entity cannot be seen directly
                }
                if (entity instanceof Player)
                {
                    user = this.um.getExactUser((Player)entity);
                    break;
                }
            }
            if (user == null)
            {
                context.sendMessage("basics", "&cNo player to kill in sight!");
                return;
            }
        }
        if (user == null)
        {
            if (!context.hasFlag("a"))
            {
                paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
            }
        }
        else
        {
            if (!user.isOnline())
            {
                illegalParameter(context, "core", "&2%s &eis currently not online!", user.getName());
            }
            if (!force && BasicsPerm.COMMAND_KILL_PREVENT.isAuthorized(user))
            {
                context.sendMessage("basics", "&cYou cannot kill that player!");
                return;
            }
            if (!force && this.basics.getBasicUserManager().getBasicUser(user).godMode)
            {
                context.sendMessage("basics", "&eThis player is in godmode you cannot kill him!");
                return;
            }
        }
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KILL_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to kill everyone!");
            }
            for (Player player : context.getCore().getUserManager().getOnlinePlayers())
            {
                if (BasicsPerm.COMMAND_KILL_PREVENT.isAuthorized(player))
                {
                    continue;
                }
                if (!player.getName().equals(context.getSender().getName()))
                {
                    if (lightning)
                    {
                        user.getWorld().strikeLightningEffect(user.getLocation());
                    }
                    user.setHealth(0);
                }
            }
        }
        else
        {
            if (lightning)
            {
                user.getWorld().strikeLightningEffect(user.getLocation());
            }
            user.setHealth(0);
            context.sendMessage("basics", "&aYou killed &2%s&a!", user.getName());
        }
    }

    @Command(desc = "Shows when given player was online the last time", min = 1, max = 1, usage = "<player>")
    public void seen(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        if (user.isOnline())
        {
            context.sendMessage("basics", "&2%s &eis currently online!", user.getName());
        }
        else
        {
            long lastPlayed = user.getLastPlayed();
            if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) // If greater than 7 days show distance not date
            {
                context.sendMessage("basics", "&2%s &eis offline since %2$td.%2$tm.%2$tY %2$tk:%2$tM", user.getName(), lastPlayed); //dd.MM.yyyy HH:mm
            }
            else
            {
                context.sendMessage("basics", "&2%s &ewas last seen %s %2$te days %2$tk hours %2$tM minutes ago.", user.getName(),
                        new Duration(System.currentTimeMillis(), lastPlayed).format("%www %ddd %hhh %mmm %sss"));
            }
        }
    }

    @Command(desc = "Makes a player execute a command", usage = "<player> <command>", min = 2, flags = @Flag(longName = "chat", name = "c"))
    public void sudo(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasArg(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        if (context.hasFlag("c"))
        {
            user.chat(sb.toString());
        }
        else
        {
            user.chat("/" + sb.toString()); //TODO later msg to sender if cmd worked??
        }
    }

    @Command(desc = "Kills yourself", max = 0)
    public void suicide(CommandContext context)
    {
        User sender = um.getExactUser(context.getSender());
        if (sender == null)
        {
            blockCommand(context, "basics", "&cYou want to kill yourself? &aThe command for that is stop!");
        }
        sender.setHealth(0);
        sender.setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, 20));
        context.sendMessage("bascics", "&eYou ended your pitiful life. &cWhy? &4:(");
    }

    @Command(desc = "Displays that you are afk", max = 0)
    public void afk(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&cJust go!");
            return;
        }
        sender.setAttribute(basics, "afk", true);
        this.um.broadcastStatus("basics", "is now afk.", context.getSender().getName());
    }

    @Command(desc = "Displays informations from a player!", usage = "<player>", min = 1)
    public void whois(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        context.sendMessage("basics", "&eNickname: &2%s", user.getName());
        context.sendMessage("basics", "&eLife: &2%d&f/&2%d", user.getHealth(), user.getMaxHealth());
        context.sendMessage("basics", "&eHunger: &2%d&f/&220 &f(&2%d&f/&2%d&f)", user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel());
        context.sendMessage("basics", "&eLevel: &2%d &f+ &2%d%%", user.getLevel(), (int)(user.getExp() * 100));
        Location loc = user.getLocation(); // NPE when user is offline
        // TODO why is this even able to be null?
        if (loc != null)
        {
            context.sendMessage("basics", "&ePosition: &2%d&f:&2%d&f:&2%d &ein &6%s\n", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
        }
        context.sendMessage("basics", "&eIP: &2%s", user.getAddress().getAddress().getHostAddress());
        context.sendMessage("basics", "&eGamemode: &2%s", user.getGameMode().toString());
        if (user.getAllowFlight())
        {
            context.sendMessage("basics", "&eFlymode: &atrue &f(%s)", user.isFlying() ? "flying" : "not flying");
        }
        else
        {
            context.sendMessage("basics", "&eFlymode: &cfalse\n");
        }
        if (user.isOp())
        {
            context.sendMessage("basics", "&eOP: &atrue");
        }
        Timestamp muted = basics.getBasicUserManager().getBasicUser(user).muted;
        if (muted != null && muted.getTime() > System.currentTimeMillis())
        {
            context.sendMessage("basics", "&eMuted: &ctrue"); //TODO show time
        }
        if (user.getGameMode() != GameMode.CREATIVE)
        {
            context.sendMessage("basics", "&eGodMode: &2%s\n", user.isInvulnerable() ? "&atrue" : "&cfalse");
        }
        if (user.getAttribute(basics, "afk") != null)
        {
            context.sendMessage("basics", "&eAFK: &atrue");
        }
        // TODO event so other modules can add their information
    }

    @Command(desc = "Toggles the god-mode!", usage = "[player]")
    public void god(CommandContext context)
    {
        User user = null;
        boolean other = false;
        if (context.hasArg(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
            }
            other = true;
            if (!BasicsPerm.COMMAND_GOD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to god others!");
            }
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            if (user == null)
            {
                context.sendMessage("basics", "&aYou are god already!");
                return;
            }
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        bUser.godMode = !bUser.godMode;
        if (bUser.godMode)
        {
            if (other)
            {
                user.sendMessage("basics", "&aYou are now invincible!");
                context.sendMessage("basics", "&2%s &ais now invincible!", user.getName());
            }
            else
            {
                context.sendMessage("basics", "&aYou are now invincible!");
            }
        }
        else
        {
            if (other)
            {
                user.sendMessage("basics", "&eYou are no longer invincible!");
                context.sendMessage("basics", "&2%s &eis no longer invincible!", user.getName());
            }
            else
            {
                context.sendMessage("basics", "&eYou are no longer invincible!");
            }
        }
    }

    @Command(desc = "Changes your walkspeed.", usage = "<speed> [player <player>]", min = 1)
    public void walkspeed(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        User user = sender;
        boolean other = false;
        if (context.hasParam("player"))
        {
            user = context.getParam("player");
            if (user != sender)
            {
                other = true;
            }
        }
        else
        { // Sender is console and no player given!
            if (sender == null)
            {
                invalidUsage(context, "basics", "&eYou suddenly feel much faster!");
            }
        }
        if (user == null)
        {
            illegalParameter(context, "core", "&cUser %s not found!", context.getString("player"));
        }
        if (!user.isOnline())
        {
            illegalParameter(context, "core", "User %s is not online!", user.getName());
        }
        // PermissionChecks
        if (other)
        {
            if (!BasicsPerm.COMMAND_WALKSPEED_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to change the walk-speed of other user!");
            }
        }
        if (!BasicsPerm.WALKSPEED_ISALLOWED.isAuthorized(user)) // if user can get his flymode changed
        {
            denyAccess(context, "The User %s is not allowed to walk faster!", user.getName());
        }
        user.setWalkSpeed(0.2f);
        Float speed = context.getArg(0, Float.class);
        if (speed != null && speed >= -10 && speed <= 10)
        {
            if (speed > 0 && speed <= 10)
            {
                user.setWalkSpeed(speed / 10f);
                user.sendMessage("basics", "You can now walk at %.2f", speed);
            }
            else
            {
                if (speed > 9000)
                {
                    user.sendMessage("basics", "&cIt's over 9000!");
                }
                else
                {
                    user.sendMessage("basics", "&eWalkspeed has to be a Number between &6-10 &eand &610&e!");
                }
            }
        }
        else
        {
            user.sendMessage("basics", "&eWalkspeed has to be a Number between &6-10 &eand &610&e!");
        }
    }
}
