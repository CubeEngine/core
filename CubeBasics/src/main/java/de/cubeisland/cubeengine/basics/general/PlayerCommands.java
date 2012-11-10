package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import net.minecraft.server.EntityPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

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

    @Command(
        desc = "Refills your hunger bar",
        max = 1,
        flags = @Flag(longName = "all", name = "a"),
        usage = "[player]|[-a]")
    public void feed(CommandContext context)
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
            this.um.broadcastMessage("basics", "&2%s &ashared food with everyone.", context.getSender().getName());
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
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

    @Command(
        desc = "Empties the hunger bar",
        max = 1,
        flags = @Flag(longName = "all", name = "a"),
        usage = "[player]|[-a]")
    public void starve(CommandContext context)
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
            this.um.broadcastMessage("basics", "&2%s &etook away all food.", context.getSender().getName());
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
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

    @Command(
        desc = "Heals a Player",
        max = 1,
        flags = @Flag(longName = "all", name = "a"),
        usage = "[player]|[-a]")
    public void heal(CommandContext context)
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
            this.um.broadcastMessage("basics", "&2%s &ahealed every player.", context.getSender().getName());
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
            {
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
                user.sendMessage("basics", "&aYou got healed by &2%s&a!", sender.getName());
            }
            else
            {
                context.sendMessage("basics", "&aYou are now healed!");
            }
        }
    }

    @Command(
        names = { "gamemode", "gm" },
        max = 2,
    desc = "Changes the gamemode",
    usage = "[gamemode] [player]")
    public void gamemode(CommandContext context)
    {
        boolean changeOther = false;
        User sender = context.getSenderAsUser();
        User user = sender;
        if (user == null)
        {
            invalidUsage(context, "basics", "&cYou do not not have any gamemode!");
        }
        if (context.hasIndexed(1))
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
        if (context.hasIndexed(0))
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
            context.sendMessage("basics", "&You changed the gamemode of &2%s &ato &6%s&a!",
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

    @Command(
        desc = "Kills a player",
    usage = "<player>|-a",
        flags = { @Flag(longName = "all", name = "a") })
    public void kill(CommandContext context)
    {//TODO kill a player looking at if possible
        //TODO kill a player with cool effects :) e.g. lightning
        User user = context.getUser(0);
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
            if (BasicsPerm.COMMAND_KILL_PREVENT.isAuthorized(user))
            {
                context.sendMessage("basics", "&cYou cannot kill that player!");
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
                    user.setHealth(0);
                }
            }
        }
        else
        {
            user.setHealth(0);
            //TODO broadcast alternative Deathmsgs
            context.sendMessage("basics", "&aYou killed &2%s&a!", user.getName());
        }
    }

    @Command(
        desc = "Makes a player execute a command",
    usage = "<player> <command>",
    min = 2,
        flags = { @Flag(longName = "chat", name = "c") })
    public void sudo(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasIndexed(i))
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

    @Command(
        desc = "Shows when given player was online the last time",
        min = 1,
        max = 1,
        usage = "<player>")
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
                context.sendMessage("basics", "&2%s &ewas last seen %2$te days %2$tk hours %2$tM minutes ago.", user.getName(), System.currentTimeMillis() - lastPlayed);
            } //TODO output formatting durations is wrong ... .(
        }
    }

    @Command(
        desc = "Kills yourself",
        max = 0)
    public void suicide(CommandContext context)
    {
        User sender = um.getExactUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context, "basics", "&cYou want to kill yourself? &aThe command for that is stop!");
        }
        sender.setHealth(0);
        sender.setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, 20));
        context.sendMessage("bascics", "&eYou ended your pitiful life. &cWhy? &4:(");
    }

    @Command(
        desc = "Displays that you are afk",
        max = 0)
    public void afk(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cJust go!");
        sender.setAttribute(basics, "afk", true);
        this.basics.getUserManager().broadcastMessage("basics", "%2%s &fis now afk.", sender.getName());
    }

    @Command(
        desc = "Displays informations from a player!",
        usage = "<player>",
        min = 1)
    public void whois(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        context.sendMessage("basics", "&eNickname: &2%s", user.getName());
        context.sendMessage("basics", "&eLife: &2%d&f/&2%d\n", user.getHealth(), user.getMaxHealth());
        context.sendMessage("basics", "&eHunger: &2%d&f/&220 &f(&2%d&f/&2%d&f)\n", user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel());
        context.sendMessage("basics", "&eLevel: &2%d &eExp: &2%d&f/&2100%% &eof the next Level\n", user.getLevel(), (int)(user.getExp() * 100));
        Location loc = user.getLocation();
        context.sendMessage("basics", "&ePosition: &2%d %d %d &ein world %2%s\n", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
        context.sendMessage("basics", "&eIP: &2%s\n", user.getAddress().getAddress().getHostAddress());
        context.sendMessage("basics", "&eGamemode: &2%s\n", user.getGameMode().toString());
        if (user.getAllowFlight())
        {
            context.sendMessage("basics", "\"&eFlymode: &atrue %f(%s)\n", user.isFlying() ? "flying" : "not flying");
        }
        else
        {
            context.sendMessage("basics", "\"&eFlymode: &cfalse\n");
        }
        context.sendMessage("basics", "&eOP: %s\n", user.isOp() ? "&atrue" : "&cfalse");
        context.sendMessage("basics", "&eMuted: %s\n",
            basics.getBasicUserManager().getBasicUser(user).muted.getTime() > System.currentTimeMillis() ? "&atrue" : "&cfalse");
        if (user.getGameMode() != GameMode.CREATIVE && user.getPlayer() instanceof CraftPlayer)
        {
            context.sendMessage("basics", "&eGodMode: &2%s\n", ((CraftPlayer)user.getPlayer()).getHandle().abilities.isInvulnerable ? "&atrue" : "&cfalse");
        }
        context.sendMessage("basics", "&eAFK: %s", user.getAttribute(basics, "afk") == null ? "&cfalse" : "&atrue");
        // TODO later money
        //TODO event so other modules can add their information
    }

    @Command(
        desc = "Toggles the god-mode!",
        usage = "[player]")
    public void god(CommandContext context)
    {
        User user;
        boolean other = false;
        if (context.hasIndexed(0))
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
            user = context.getSenderAsUser("basics", "&aYou are god already!");
        }
        EntityPlayer player = ((CraftPlayer)user.getPlayer()).getHandle();
        player.abilities.isInvulnerable = !player.abilities.isInvulnerable;
        if (player.abilities.isInvulnerable)
        {
            if (other)
            {
                context.sendMessage("basics", "&aYou are now invincible!");
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
                context.sendMessage("basics", "&eYou are no longer invincible!");
                context.sendMessage("basics", "&2%s &eis no longer invincible!", user.getName());
            }
            else
            {
                context.sendMessage("basics", "&eYou are no longer invincible!");
            }
        }
    }
}