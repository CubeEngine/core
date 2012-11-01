package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class GeneralCommands
{
    private UserManager um;
    private Basics module;
    private String lastWhisperOfConsole = null;

    public GeneralCommands(Basics module)
    {
        this.module = module;
        this.um = module.getUserManager();
    }

    @Command(
    desc = "Allows you to emote",
    min = 1,
    usage = "<message>")
    public void me(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        this.um.
            broadcastMessage("basics", "* %s %s", context.getSender().getName(), sb.toString()); // Here no category so -> no Translation
    }

    @Command(
    desc = "Sends a private message to someone",
    names =
    {
        "message", "msg", "tell", "pn", "m", "t", "whisper"
    },
    min = 1,
    usage = "<player> <message>")
    public void msg(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        User sender = context.getSenderAsUser();
        User user = context.getUser(0);
        if (user == null)
        {
            if (sender == null)
            {
                illegalParameter(context, "basics", "&eTalking to yourself?");
            }
            if (context.getString(0).equalsIgnoreCase("console"))
            {   // TODO find why console does not get any message here:
                context.getSender().getServer().getConsoleSender().sendMessage(
                    _("basics", "&e%s -> You: &f%s", context.getSender().getName(), sb));
                context.sendMessage("basics", "&eYou -> %s: &f%s", "CONSOLE", sb);
            }
            else
            {
                invalidUsage(context, "core", "User not found!");
            }
        }
        else
        {
            if (sender == user)
            {
                illegalParameter(context, "basics", "&eTalking to yourself?");
            }
            user.sendMessage("basics", "&e%s -> You: &f%s", context.getSender().getName(), sb);
            context.sendMessage(_("basics", "&eYou -> %s: &f%s", user.getName(), sb));
        }

        if (sender == null)
        {
            this.lastWhisperOfConsole = user.getName();
            user.setAttribute(module,
                "lastWhisper", "console");
        }
        else
        {
            if (user == null)
            {
                this.lastWhisperOfConsole = sender.getName();
                sender.setAttribute(module, "lastWhisper", "console");
            }
            else
            {
                sender.setAttribute(module, "lastWhisper", user.getName());
                user.setAttribute(module, "lastWhisper", sender.getName());
            }
        }
    }

    @Command(
    names =
    {
        "reply", "r"
    },
    desc = "Replies to the last person that whispered to you.",
    usage = "<message>")
    public void reply(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        boolean replyToConsole = false;
        User user;
        String lastWhisperer;
        if (sender == null)
        {
            if (this.lastWhisperOfConsole == null)
            {
                invalidUsage(context, "basics", "Nobody send you a message you could reply to!");
            }
            lastWhisperer = lastWhisperOfConsole;
        }
        else
        {
            lastWhisperer = sender.getAttribute(module,"lastWhisper");
            if (lastWhisperer == null)
            {
                invalidUsage(context, "basics", "Nobody send you a message you could reply to!");
                return;
            }
            replyToConsole = "console".equalsIgnoreCase(lastWhisperer);
        }
        user = um.findUser(lastWhisperer);
        if (!replyToConsole && (user == null || !user.isOnline()))
        {
            invalidUsage(context, "basics", "Could not find the player to reply too. Is he offline?");
        }
        StringBuilder sb = new StringBuilder(); //TODO absolutly need this in cmdContext i am using it way too often
        int i = 0;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        if (replyToConsole)
        {
            sender.getServer().getConsoleSender().
                sendMessage(_("basics", "&e%s -> You: &f%s", context.getSender().
                getName(), sb.toString()));
            context.sendMessage("basics", "&eYou -> %s: &f%s", "CONSOLE", sb.
                toString());
        }
        else
        {
            user.sendMessage("basics", "&e%s -> You: &f%s", context.getSender().
                getName(), sb);
            context.
                sendMessage(_("basics", "&eYou -> %s: &f%s", user.getName(), sb));
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
        desc = "Displays the direction in which you are looking.")
    public void compass(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "I assume you are looking right at your server-console. Right?");
        final int direction = (int)(sender.getLocation().getYaw() + 180 + 360) % 360;
        //TODO any idea to do this better?
        String dir;
        if (direction < 23)
        {
            dir = "N";
        }
        else
        {
            if (direction < 68)
            {
                dir = "NE";
            }
            else
            {
                if (direction < 113)
                {
                    dir = "E";
                }
                else
                {
                    if (direction < 158)
                    {
                        dir = "SE";
                    }
                    else
                    {
                        if (direction < 203)
                        {
                            dir = "S";
                        }
                        else
                        {
                            if (direction < 248)
                            {
                                dir = "SW";
                            }
                            else
                            {
                                if (direction < 293)
                                {
                                    dir = "W";
                                }
                                else
                                {
                                    if (direction < 338)
                                    {
                                        dir = "NW";
                                    }
                                    else
                                    {
                                        dir = "N";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        sender.sendMessage("basics", "You are looking into %s", _(sender, "basics", dir));
    }

    @Command(
        desc = "Displays your current depth.")
    public void depth(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "You dug too deep!");
        int height = sender.getLocation().getBlockY();
        if (height > 62)
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d above sealevel)", height, height - 62);
        }
        else
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d below sealevel)", height, 62 - height);
        }
    }

    @Command(
        desc = "Displays your current location.")
    public void getPos(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "Your position: Right in front of your screen!");
        sender.sendMessage("basics", "Your position is X:%d Y:%d Z:%d", sender.getLocation().getBlockX(), sender.getLocation().getBlockY(), sender.getLocation().getBlockZ());
    }

    @Command(
        desc = "Looks up an item for you!",
    max = 1,
    usage = "<item>")
    public void itemDB(CommandContext context)
    {
        ItemStack item = MaterialMatcher.get().matchItemStack(context.getString(0));
        if (item != null)
        {
            context.sendMessage("basics", "Found %s (%d:%d)", MaterialMatcher.get().getNameFor(item), item.getType().getId(), item.getDurability());
        }
        else
        {
            context.sendMessage("basics", "Could not find any item named %s", context.getString(0));
        }
    }

    @Command(
        desc = "Displays all the online players.")
    public void list(CommandContext context)
    {
        //TODO do not show hidden players
        //TODO possibility to show prefix or main role etc.
        //TODO softdependency with Roles/etc for grouped output
        //Players online: x/x
        List<Player> players = context.getCore().getUserManager().getOnlinePlayers();
        if (players.isEmpty())
        {
            context.sendMessage("basics", "&cThere are no players online now!");
            return;
        }
        context.sendMessage("basics", "&9Players online: &a%d&f/&e%d", players.size(), context.getCore().getServer().getMaxPlayers());
        context.sendMessage("basics", "&ePlayers:\n&2%s", this.displayPlayerList(players));
    }

    public String displayPlayerList(List<Player> players)
    {
        //TODO test if it looks good for more players
        //1 line ~ 70 characters
        //6 12 18 (+1)
        StringBuilder sb = new StringBuilder();
        StringBuilder partBuilder = new StringBuilder();
        int pos = 0;
        boolean first = true;
        for (Player player : players)
        {
            partBuilder.setLength(0);

            String name = player.getName();
            if (name.length() < 6)
            {
                int k = 6 - name.length();
                partBuilder.append(StringUtils.repeat(" ", k / 2));
                k = k - k / 2;
                partBuilder.append(name);
                partBuilder.append(StringUtils.repeat(" ", k));
                pos += 6;
            }
            else
            {
                if (name.length() < 12)
                {
                    int k = 12 - name.length();
                    partBuilder.append(StringUtils.repeat(" ", k / 2));
                    k = k - k / 2;
                    partBuilder.append(name);
                    partBuilder.append(StringUtils.repeat(" ", k));
                    pos += 12;
                }
                else
                {
                    int k = 16 - name.length();
                    partBuilder.append(StringUtils.repeat(" ", k / 2));
                    k = k - k / 2;
                    partBuilder.append(name);
                    partBuilder.append(StringUtils.repeat(" ", k));
                    pos += 16;
                }
            }
            if (pos >= 30)
            {
                pos = partBuilder.toString().length();
                sb.append("\n");
                first = true;
            }
            if (!first)
            {
                sb.append("&f|&2");
                pos++;
            }
            sb.append(partBuilder.toString());
            first = false;
        }
        return sb.toString();
    }

    @Command(desc = "Displays the message of the day!")
    public void motd(CommandContext context)
    {
        context.sendMessage(module.getConfiguration().motd);
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
    /**
     *
     * DONE: (or almost)
     *
     * afk
     * compass
     * depth
     * getpos
     * me
     * msg / r
     * seen
     * suicide
     * itemdb (items.csv or something like that)
     * list
     * motd
     * whois
     *
     * //TODO
     *
     * kit
     *
     *
     * mail
     *
     * helpop -> move to CubePermissions ?? not only op but also "Moderator"
     * ignore -> move to CubeChat
     * info
     *
     *
     * near
     * nick -> move to CubeChat
     * pt
     * realname -> move to CubeChat
     * rules
     *
     * help -> Display ALL availiable cmd
     *
     *
     */
}