package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
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
    flags =
    {
        @Flag(longName = "all", name = "a")
    },
    usage = "[player]")
    public void feed(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().
                getOnlinePlayers();
            for (Player player : players)
            {
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            context.sendMessage("basics", "You made everyone fat!");
            this.um.
                broadcastMessage("basics", "%s shared food with everyone.", context.
                getSender().getName());
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
                    invalidUsage(context, "core", "User not found!");
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "&cDon't feed the troll!");
                }
            }
            user.setFoodLevel(20);
            user.setSaturation(20);
            user.setExhaustion(0);
            if (other)
            {
                context.sendMessage("basics", "&6Feeded %s", user.getName());
                user.sendMessage("basics", "&6You got fed by %s", context.
                    getSender().getName());
            }
            else
            {
                context.sendMessage("basics", "&6You are now fed!");
            }
        }
    }

    @Command(
    desc = "Empties the hunger bar",
    max = 1,
    flags =
    {
        @Flag(longName = "all", name = "a")
    },
    usage = "[player]")
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
            context.sendMessage("basics", "You starve everyone to death!");
            this.um.broadcastMessage("basics", "%s took away all food.", context.getSender().getName());
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
                    invalidUsage(context, "core", "User not found!");
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "\n\n\n\n\n&cI'll give you only one line to eat!\n\n\n\n\n");
                }
            }
            user.setFoodLevel(0);
            user.setSaturation(0);
            user.setExhaustion(4);
            if (other)
            {
                context.sendMessage("basics", "&6Starved %s", user.getName());
                user.sendMessage("basics", "&6You are suddenly starving!");
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
    flags =
    {
        @Flag(longName = "all", name = "a")
    },
    usage = "[player]|-a")
    public void heal(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().
                getOnlinePlayers();
            for (Player player : players)
            {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            context.sendMessage("basics", "You healed everyone!");
            this.um.
                broadcastMessage("basics", "%s healed every player.", context.
                getSender().getName());
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
                    invalidUsage(context, "core", "User not found!");
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
                context.sendMessage("basics", "&6Healed %s", user.getName());
                user.sendMessage("basics", "&6You got healed by %s", sender.
                    getName());
            }
            else
            {
                context.sendMessage("basics", "&6You are now healed!");
            }
        }
    }

    @Command(
    names =
    {
        "gamemode", "gm"
    },
    max = 2,
    desc = "Changes the gamemode",
    usage = "<gamemode> [player]")
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
                invalidUsage(context, "core", "User not found!");
            }
            changeOther = true;
        }
        if (!BasicsPerm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            denyAccess(context, "basics", "You do not have permission to change the gamemode of an other player!");
        }
        if (context.hasIndexed(0))
        {
            String mode = context.getString(0);
            if (mode.equals("survival") || mode.equals("s"))
            {
                user.setGameMode(GameMode.SURVIVAL);
            }
            else
            {
                if (mode.equals("creative") || mode.equals("c"))
                {
                    user.setGameMode(GameMode.CREATIVE);
                }
                else
                {
                    if (mode.equals("adventure") || mode.equals("a"))
                    {
                        user.setGameMode(GameMode.ADVENTURE);
                    }
                }
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
            context.
                sendMessage("basics", "You changed the gamemode of %s to %s", user.
                getName(), _(sender, "basics", user.getGameMode().toString()));
            user.
                sendMessage("basics", "Your Gamemode has been changed to %s", _(user, "basics", user.
                getGameMode().toString()));
        }
        else
        {
            context.
                sendMessage("basics", "You changed your gamemode to %s", _(user, "basics", user.
                getGameMode().toString()));
        }
    }
    
    @Command(
    desc = "Kills a player",
    usage = "<player>|-a",
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void kill(CommandContext context)
    {//TODO kill a player looking at if possible
        //TODO kill a player with cool effects :) e.g. lightnin
        User user = context.getUser(0);
        if (user == null)
        {
            if (!context.hasFlag("a"))
            {
                invalidUsage(context, "core", "User not found!");
            }
        }
        else
        {
            if (!user.isOnline())
            {
                illegalParameter(context, "core", "%s currently not online", user.getName());
            }
            if (BasicsPerm.COMMAND_KILL_PREVENT.isAuthorized(user))
            {
                context.sendMessage("basics", "You cannot kill that player!");
                return;
            }
        }
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KILL_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "You are not allowed to kill everyone!");
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
            context.sendMessage("basics", "You killed %s!", user.getName());
        }
    }
    
    @Command(
    desc = "Makes a player execute a command",
    usage = "<player> <command>",
    flags =
    {
        @Flag(longName = "chat", name = "c")
    })
    public void sudo(CommandContext context)
    {
        //User sender = context.getSenderAsUser();
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "User not found!");
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
    
    @Command(desc = "Shows when given player was online the last time",
    min = 1,
    max = 1,
    usage = "<player>")
    public void seen(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        if (user.isOnline())
        {
            context.sendMessage("basics", "%s is currently online!", user.getName());
        }
        else
        {
            long lastPlayed = user.getLastPlayed();
            if (System.currentTimeMillis() - lastPlayed > 7 * 24 * 60 * 60 * 1000) // If greater than 7 days show distance not date
            {
                context.sendMessage("basics", "%s is offline since %2$td.%2$tm.%2$tY %2$tk:%2$tM", user.getName(), lastPlayed); //dd.MM.yyyy HH:mm
            }
            else
            {
                context.sendMessage("basics", "%s was last seen %2$te days %2$tk hours %2$tM minutes ago.", user.getName(), System.currentTimeMillis() - lastPlayed);
            } //TODO output formatting durations is wrong ... .(
        }
    }

    @Command(desc = "Kills yourself",
    max = 0)
    public void suicide(CommandContext context)
    {
        User sender = um.getExactUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context, "basics", "&cYou want to kill yourself? &aThe command for that is stop!");
        }
        sender.setHealth(0);
        sender.
            setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, 20));
        context.sendMessage("bascics", "You ended your pitiful life. Why? :(");
    }

    public void afk(CommandContext context)
    {
        //TODO automatic afk detection / when moving un-afk the player
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
        context.sendMessage("basics", "&eNickname: &2%s\n"
            + "&eLife: &2%d&f/&2%d\n"
            + "&eHunger: &2%d&f/&220 &f(&2%d&f/&2%d&f)\n"
            + "&eLevel: &2%d &eExp: &2%d&f/&2100%% &eof the next Level\n"
            + "&ePosition: &2%d %d %d &ein world %2%s\n"
            + "&eIP: &2%s\n"
            + "&eGamemode: &2%s\n"
            + "&eFlymode: &2%s\n"
            + "&eOP: &2%s",
            user.getName(),
            user.getHealth(), user.getMaxHealth(),
            user.getFoodLevel(), (int)user.getSaturation(), user.getFoodLevel(),
            user.getLevel(), (int)(user.getExp() * 100),
            user.getLocation().getBlockX(), user.getLocation().getBlockY(), user.getLocation().getBlockZ(), user.getLocation().getWorld().getName(),
            user.getAddress().getAddress().getHostAddress(),
            user.getGameMode().toString(),
            String.valueOf(user.isFlying()),
            String.valueOf(user.isOp()));
        /* TODO
         * (money)
         * afk
         * (godmode)
         * (muted)
         */
    }
    
    @Command(
    desc = "Displays your current language setting.",
    max = 0)
    public void language(CommandContext context)
    {
        context.sendMessage("basics", "Your language is %s.",
            context.getSenderAsUser("basics", "Your language is %s.", context.getCore().getI18n().getDefaultLanguage()).getLanguage());
    }
}
