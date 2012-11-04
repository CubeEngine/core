package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class ChatCommands
{
    private UserManager um;
    private String lastWhisperOfConsole = null;
    private Basics basics;

    public ChatCommands(Basics basics)
    {
        this.basics = basics;
        this.um = basics.getUserManager();
    }
    //TODO mute? or cubechat

    @Command(
    desc = "Allows you to emote",
    min = 1,
    usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.um.broadcastMessage("basics", "* %s %s", context.getSender().getName(), message); // Here no category so -> no Translation
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
        String message = context.getStrings(1);
        User sender = context.getSenderAsUser();
        User user = context.getUser(0);
        if (user == null)
        {
            if (sender == null)
            {
                illegalParameter(context, "basics", "&eTalking to yourself?");
            }
            if (context.getString(0).equalsIgnoreCase("console"))
            {
                context.getSender().getServer().getConsoleSender().
                    sendMessage(_("basics", "&e%s -> You: &f%s", context.getSender().getName(), message));
                context.sendMessage("basics", "&eYou -> %s: &f%s", "CONSOLE", message);
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
            user.sendMessage("basics", "&e%s -> You: &f%s", context.getSender().getName(), message);
            context.sendMessage(_("basics", "&eYou -> %s: &f%s", user.getName(), message));
        }

        if (sender == null)
        {
            this.lastWhisperOfConsole = user.getName();
            user.setAttribute(basics,
                "lastWhisper", "console");
        }
        else
        {
            if (user == null)
            {
                this.lastWhisperOfConsole = sender.getName();
                sender.setAttribute(basics, "lastWhisper", "console");
            }
            else
            {
                sender.setAttribute(basics, "lastWhisper", user.getName());
                user.setAttribute(basics, "lastWhisper", sender.getName());
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
            lastWhisperer = sender.getAttribute(basics, "lastWhisper");
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
        String message = context.getStrings(0);
        if (replyToConsole)
        {
            sender.getServer().getConsoleSender().sendMessage(_("basics", "&e%s -> You: &f%s", context.getSender().getName(), message));
            context.sendMessage("basics", "&eYou -> %s: &f%s", "CONSOLE", message);
        }
        else
        {
            user.sendMessage("basics", "&e%s -> You: &f%s", context.getSender().getName(), message);
            context.sendMessage(_("basics", "&eYou -> %s: &f%s", user.getName(), message));
        }
    }

    @Command(
    desc = "Broadcasts a message",
    usage = "<message>")
    public void broadcast(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        this.um.broadcastMessage("basics", "&2[&cBroadcast&2] &e" + sb.
            toString());
    }
}
