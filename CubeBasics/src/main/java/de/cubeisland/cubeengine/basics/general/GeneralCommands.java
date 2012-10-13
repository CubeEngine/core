package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author Anselm Brehme
 */
public class GeneralCommands
{
    private UserManager um;
    private String lastWhisperOfConsole = null;

    public GeneralCommands(Basics module)
    {
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
            broadcastMessage("", "* %s %s", context.getSender().getName(), sb.
            toString()); // Here no category so -> no Translation
    }

    @Command(
    desc = "Sends a private message to someone",
    names =
    {
        "msg", "tell", "pn", "m", "t", "whisper"
    },
    min = 1,
    usage = "<player> <message>")
    public void msg(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++));
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
                context.getSender().getServer().getConsoleSender().
                    sendMessage(_("basics", "&e%s -> You: &f%s", context.
                    getSender().getName(), sb));
                context.
                    sendMessage("basics", "&eYou -> %s: &f%s", "CONSOLE", sb);
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
            user.sendMessage("basics", "&e%s -> You: &f%s", context.getSender().
                getName(), sb);
            context.
                sendMessage(_("basics", "&eYou -> %s &f%s", user.getName(), sb));
        }

        if (sender == null)
        {
            this.lastWhisperOfConsole = user.getName();
            user.setAttribute("lastWhisper", "console");
        }
        else
        {
            if (user == null)
            {
                this.lastWhisperOfConsole = sender.getName();
                sender.setAttribute("lastWhisper", "console");
            }
            else
            {
                sender.setAttribute("lastWhisper", user.getName());
                user.setAttribute("lastWhisper", sender.getName());
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
            lastWhisperer = sender.getAttribute("lastWhisper");
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
            sb.append(context.getString(i++));
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
        User sender = um.getUser(context.getSender());
        User user = context.getUser(0);
        long lastPlayed = user.getLastPlayed();
        //TODO ausgabe;       
    }

    @Command(desc = "Kills yourself",
    max = 0)
    public void suicide(CommandContext context)
    {
        User sender = um.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context, "basics", "&cYou want to kill yourself? &aThe command for that is stop!");
        }
        sender.setHealth(0);
        sender.
            setLastDamageCause(new EntityDamageEvent(sender, EntityDamageEvent.DamageCause.CUSTOM, 20));
        //TODO msg;
    }
}
