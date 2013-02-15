package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.sql.Timestamp;
import java.util.TreeSet;

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
            return;
        }
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
                context.sendMessage("basics", "&cUser %s not found!", context.getString(0));
                return;
            }
            other = true;
        }
        else if (sender == null)
        {
            context.sendMessage("basics", "&cDon't feed the troll!");
            return;
        }
        user.setFoodLevel(20);
        user.setSaturation(20);
        user.setExhaustion(0);
        if (other)
        {
            context.sendMessage("basics", "&aFeeded &2%s&a!", user.getName());
            user.sendMessage("basics", "&aYou got fed by &2%s&a!", context.getSender().getName());
            return;
        }
        context.sendMessage("basics", "&aYou are now fed!");
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
            return;
        }
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
                context.sendMessage("core", "&cUser %s not found!", context.getString(0));
                return;
            }
            other = true;
        }
        else if (sender == null)
        {
            context.sendMessage("basics", "\n\n\n\n\n\n\n\n\n\n\n\n\n&cI'll give you only one line to eat!");
            return;
        }
        user.setFoodLevel(0);
        user.setSaturation(0);
        user.setExhaustion(4);
        if (other)
        {
            context.sendMessage("basics", "&eStarved &2%s&e!", user.getName());
            user.sendMessage("basics", "&eYou are suddenly starving!");
            return;
        }
        context.sendMessage("basics", "&6You are now starving!");
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
            return;
        }
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
                context.sendMessage("core", "&cUser %s not found!", context.getString(0));
                return;
            }
            other = true;
        }
        else if (sender == null)
        {
            context.sendMessage("basics", "&cOnly time can heal your wounds!");
            return;
        }
        user.setHealth(user.getMaxHealth());
        user.setFoodLevel(20);
        user.setSaturation(20);
        user.setExhaustion(0);
        if (other)
        {
            context.sendMessage("basics", "&aHealed &2%s&a!", user.getName());
            user.sendMessage("basics", "&aYou got healed by &2%s&a!", context.getSender().getName());
            return;
        }
        context.sendMessage("basics", "&aYou are now healed!");
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
            context.sendMessage("basics", "&cYou do not not have any gamemode!");
            return;
        }
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendMessage("core", "&cUser %s not found!", context.getString(1));
                return;
            }
            changeOther = true;
        }
        if (!BasicsPerm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            context.sendMessage("basics", "&cYou are not allowed to change the gamemode of an other player!");
            return;
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
            return;
        }
            context.sendMessage("basics", "&aYou changed your gamemode to &6%s&a!",
                    _(user, "basics", user.getGameMode().toString()));
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
            if (context.getSender() instanceof User)
            {
                User sender = (User)context.getSender();
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
                this.kill(user,lightning,context,false);
                return;
            }
            context.sendMessage("basics", "&cPlease speicify a victim!");
            return;
        }
        if (user == null && !context.hasFlag("a"))
        {
            context.sendMessage("core", "&cUser %s not found!", context.getString(0));
            return;
        }
        if (!user.isOnline())
        {
            context.sendMessage("core", "&2%s &eis currently not online!", user.getName());
            return;
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
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KILL_ALL.isAuthorized(context.getSender()))
            {
                context.sendMessage("basics", "&cYou are not allowed to kill everyone!");
                return;
            }
            for (Player player : context.getCore().getUserManager().getOnlinePlayers())
            {
                if (!force && BasicsPerm.COMMAND_KILL_PREVENT.isAuthorized(player))
                {
                    continue;
                }
                if (!player.getName().equals(context.getSender().getName()))
                {
                    this.kill(player, lightning, context, false);
                }
            }
            return;
        }
        if (lightning)
        {
            user.getWorld().strikeLightningEffect(user.getLocation());
        }
        user.setHealth(0);
        context.sendMessage("basics", "&aYou killed &2%s&a!", user.getName());
    }

    private void kill(Player player, boolean lightning, ParameterizedContext context, boolean showMessage) {
        if (lightning)
        {
            player.getWorld().strikeLightningEffect(player.getLocation());
        }
        player.setHealth(0);
        if (showMessage)
        {
            context.sendMessage("basics", "&aYou killed &2%s&a!", player.getName());
        }
    }

    @Command(desc = "Shows when given player was online the last time", min = 1, max = 1, usage = "<player>")
    public void seen(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("basics", "&cUser %s not found!", context.getString(0));
            return;
        }
        if (user.isOnline())
        {
            context.sendMessage("basics", "&2%s &eis currently online!", user.getName());
            return;
        }
        long lastPlayed = user.getLastPlayed();
        if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) // If greater than 7 days show distance not date
        {
            context.sendMessage("basics", "&2%s &eis offline since %2$td.%2$tm.%2$tY %2$tk:%2$tM", user.getName(), lastPlayed); //dd.MM.yyyy HH:mm
            return;
        }
        context.sendMessage("basics", "&2%s &ewas last seen %s %2$te days %2$tk hours %2$tM minutes ago.", user.getName(),
            new Duration(System.currentTimeMillis(), lastPlayed).format("%www %ddd %hhh %mmm %sss"));
    }

    @Command(desc = "Makes a player execute a command", usage = "<player> <command>", min = 2, flags = @Flag(longName = "chat", name = "c"))
    public void sudo(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("core", "&cUser %s not found!", context.getString(0));
            return;
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
            return;
        }
        this.basics.getCommandManager().runCommand(user,sb.toString());
    }

    @Command(desc = "Kills yourself", max = 0)
    public void suicide(CommandContext context)
    {
        User sender = um.getExactUser(context.getSender());
        if (sender == null)
        {
            context.sendMessage("basics", "&cYou want to kill yourself? &aThe command for that is stop!");
            return;
        }
        sender.setHealth(0);
        sender.setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, 20));
        context.sendMessage("bascics", "&eYou ended your pitiful life. &cWhy? &4:(");
    }

    @Command(desc = "Displays that you are afk", max = 0)
    public void afk(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.setAttribute(basics, "afk", true);
            this.um.broadcastStatus("basics", "is now afk.", context.getSender().getName());
            return;
        }
        context.sendMessage("basics", "&cJust go!");
    }

    @Command(desc = "Displays informations from a player!", usage = "<player>", min = 1)
    public void whois(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("basics", "User not found!");
            return;
        }
        context.sendMessage("basics", "&eNickname: &2%s", user.getName());
        context.sendMessage("basics", "&eLife: &2%d&f/&2%d", user.getHealth(), user.getMaxHealth());
        context.sendMessage("basics", "&eHunger: &2%d&f/&220 &f(&2%d&f/&2%d&f)", user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel());
        context.sendMessage("basics", "&eLevel: &2%d &f+ &2%d%%", user.getLevel(), (int)(user.getExp() * 100));
        Location loc = user.getLocation();
        if (loc != null)
        {
            context.sendMessage("basics", "&ePosition: &2%d&f:&2%d&f:&2%d &ein &6%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
        }
        if (user.getAddress() != null)
        {
            context.sendMessage("basics", "&eIP: &2%s", user.getAddress().getAddress().getHostAddress());
        }
        if (user.getGameMode() != null)
        {
            context.sendMessage("basics", "&eGamemode: &2%s", user.getGameMode().toString());
        }
        if (user.getAllowFlight())
        {
            context.sendMessage("basics", "&eFlymode: &atrue &f(%s)", user.isFlying() ? "flying" : "not flying");
        }
        else
        {
            context.sendMessage("basics", "&eFlymode: &cfalse");
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
            context.sendMessage("basics", "&eGodMode: &2%s", user.isInvulnerable() ? ChatFormat.BRIGHT_GREEN+"true" : ChatFormat.RED+"false");
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
                context.sendMessage("basics", "&cUser %s not found!", context.getString(0));
                return;
            }
            other = true;
            if (!BasicsPerm.COMMAND_GOD_OTHER.isAuthorized(context.getSender()))
            {
                context.sendMessage("basics", "&cYou are not allowed to god others!");
                return;
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
                return;
            }
            context.sendMessage("basics", "&aYou are now invincible!");
            return;
        }
        if (other)
        {
            user.sendMessage("basics", "&eYou are no longer invincible!");
            context.sendMessage("basics", "&2%s &eis no longer invincible!", user.getName());
            return;
        }
        context.sendMessage("basics", "&eYou are no longer invincible!");
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
        else if (sender == null) // Sender is console and no player given!
        {
            context.sendMessage("basics", "&eYou suddenly feel much faster!");
            return;
        }
        if (user == null)
        {
            context.sendMessage("core", "&cUser %s not found!", context.getString("player"));
            return;
        }
        if (!user.isOnline())
        {
            context.sendMessage("core", "User %s is not online!", user.getName());
            return;
        }
        // PermissionChecks
        if (other && !BasicsPerm.COMMAND_WALKSPEED_OTHER.isAuthorized(context.getSender()))
        {
            context.sendMessage("basics", "&cYou are not allowed to change the walk-speed of other user!");
            return;
        }
        if (!BasicsPerm.WALKSPEED_ISALLOWED.isAuthorized(user)) // if user can get his flymode changed
        {
            context.sendMessage("The User %s is not allowed to walk faster!", user.getName());
            return;
        }
        user.setWalkSpeed(0.2f);
        Float speed = context.getArg(0, Float.class);
        if (speed != null && speed >= -10 && speed <= 10)
        {
            if (speed > 0 && speed <= 10)
            {
                user.setWalkSpeed(speed / 10f);
                user.sendMessage("basics", "You can now walk at %.2f", speed);
                return;
            }
            if (speed > 9000)
            {
                user.sendMessage("basics", "&cIt's over 9000!");
                return;
            }
            user.sendMessage("basics", "&eWalkspeed has to be a Number between &6-10 &eand &610&e!");
            return;
        }
        user.sendMessage("basics", "&eWalkspeed has to be a Number between &6-10 &eand &610&e!");
    }
}
