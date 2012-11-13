package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.args.LongArg;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.sql.Timestamp;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
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

    @Command(
        desc = "Allows you to emote",
        min = 1,
        usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.um.broadcastMessage("basics", "* &2%s &f%s", context.getSender().getName(), message); // Here no category so -> no Translation
    }

    @Command(
        desc = "Sends a private message to someone",
        names = { "message", "msg", "tell", "pn", "m", "t", "whisper" },
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
                    sendMessage(_("basics", "&2%s &6-> &eYou: &f%s", context.getSender().getName(), message));
                context.sendMessage("basics", "&eYou &6-> &2%s&e: &f%s", "CONSOLE", message);
            }
            else
            {
                paramNotFound(context, "core", "&cUser %s not found!", context.getString(0));
            }
        }
        else
        {
            if (sender == user)
            {
                paramNotFound(context, "basics", "&eTalking to yourself?");
            }
            user.sendMessage("basics", "&2%s &6-> &eYou: &f%s", context.getSender().getName(), message);
            context.sendMessage(_("basics", "&eYou &6-> &2%s&e: &f%s", user.getName(), message));
        }

        if (sender == null)
        {
            this.lastWhisperOfConsole = user.getName();
            user.setAttribute(basics, "lastWhisper", "console");
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
        names = { "reply", "r" },
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
                blockCommand(context, "basics", "&eNobody send you a message you could reply to!");
            }
            lastWhisperer = lastWhisperOfConsole;
        }
        else
        {
            lastWhisperer = sender.getAttribute(basics, "lastWhisper");
            if (lastWhisperer == null)
            {
                blockCommand(context, "basics", "&eNobody send you a message you could reply to!");
                return;
            }
            replyToConsole = "console".equalsIgnoreCase(lastWhisperer);
        }
        user = um.findUser(lastWhisperer);
        if (!replyToConsole && (user == null || !user.isOnline()))
        {
            paramNotFound(context, "basics", "&eCould not find the player to reply too. Is he offline?");
        }
        String message = context.getStrings(0);
        if (replyToConsole)
        {
            sender.getServer().getConsoleSender().sendMessage(_("basics", "&e%s -> You: &f%s", context.getSender().getName(), message));
            context.sendMessage("basics", "&eYou &6-> &2%s&e: &f%s", "CONSOLE", message);
        }
        else
        {
            user.sendMessage("basics", "&2%s &6-> &eYou: &f%s", context.getSender().getName(), message);
            context.sendMessage(_("basics", "&eYou &6-> %&2s&e: &f%s", user.getName(), message));
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
        this.um.broadcastMessage("basics", "&2[&cBroadcast&2] &e" + sb.toString());
    }

    @Command(
        desc = "Mutes a player",
        usage = "<player> [duration]",
        min = 1,
        max = 2)
    public void mute(CommandContext context) // mute will be unset when user is unloaded
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        if (bUser.muted != null && bUser.muted.getTime() < System.currentTimeMillis())
        {
            context.sendMessage("basics", "&2%s &ewas already muted!", user.getName());
        }
        long delay = basics.getConfiguration().defaultMuteTime * 1000 * 60; //TODO use other format
        if (delay < 1)
        {
            delay = System.currentTimeMillis() + 31104000000000L; // ~ 1k years
        }
        if (context.hasIndexed(1))
        {
            delay = context.getIndexed(1, LongArg.class, delay);
        }
        bUser.muted = new Timestamp(System.currentTimeMillis() + delay);
        this.basics.getBasicUserManager().update(bUser);
        user.sendMessage("basics", "&cYou are now muted for &6%d &cseconds!",(int)delay/20);//TODO message / time value
        context.sendMessage("basics","&eYou muted &2%s &efor &6%d &esec!",user.getName(),(int)delay/20);
    }

    @Command(
        desc = "Unmutes a player",
        usage = "<player>",
        min = 1,
        max = 1)
    public void unmute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        bUser.muted = null;
        this.basics.getBasicUserManager().update(bUser);
        context.sendMessage("basics", "&2%s &ais not muted now!", user.getName());
    }
}