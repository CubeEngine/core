package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.util.Misc.arr;

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

    @Command(desc = "Ignores all messages from players", min = 1, max = 1, usage = "<player>")
    public void ignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendMessage("basics","&cUser %s not found!",context.getString(0));
                return;
            }
            if (this.basics.getIgnoreListManager().addIgnore(sender, user))
            {
                context.sendMessage("basics", "&aSuccesfully added &2%s &ato your ignore-list", user.getName());
                return;
            }
            context.sendMessage("basics", "&cYou already ignored &2%s&c!", user.getName());
            return;
        }
        context.sendMessage("basics", "&cThis command is not availiable for console!");
    }

    @Command(desc = "Ignores all messages from players", min = 1, max = 1, usage = "<player>")
    public void unignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendMessage("basics","&cUser %s not found!",context.getString(0));
                return;
            }
            if (this.basics.getIgnoreListManager().removeIgnore(sender, user))
            {
                context.sendMessage("basics", "&aSuccesfully removed &2%s &afrom your ignore-list", user.getName());
                return;
            }
            context.sendMessage("basics", "&cYou haven't ignored &2%s&c!", user.getName());
            return;
        }
        context.sendMessage("basics", "&cThis command is not availiable for console!");

    }

    @Command(desc = "Allows you to emote", min = 1, usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.um.broadcastStatus(message, context.getSender().getName());
    }

    @Command(desc = "Sends a private message to someone", names = {
        "message", "msg", "tell", "pm", "m", "t", "whisper"
    }, min = 2, usage = "<player> <message>")
    public void msg(CommandContext context)
    {
        if (!this.sendWhisperTo(context.getString(0),context.getStrings(1),context))
        {
            context.sendMessage("basics", "&eCould not find the player &2%s &eto send the message. Is he offline?", context.getString(0));
        }
    }

    @Command(names = {
        "reply", "r"
    }, desc = "Replies to the last person that whispered to you.", usage = "<message>")
    public void reply(CommandContext context)
    {
        String lastWhisper;
        if (context.getSender() instanceof User)
        {
            lastWhisper = ((User)context.getSender()).getAttribute(basics, "lastWhisper");
        }
        else
        {
            lastWhisper = lastWhisperOfConsole;
        }
        if (lastWhisper == null)
        {
            context.sendMessage("basics", "&eNobody send you a message you could reply to!");
            return;
        }
        if (!this.sendWhisperTo(lastWhisper, context.getStrings(0), context))
        {
            context.sendMessage("basics", "&eCould not find the player &2%s &eto reply too. Is he offline?", lastWhisper);
        }
    }

    private boolean sendWhisperTo(String whisperTarget, String message, CommandContext context)
    {
        User user = um.findUser(whisperTarget);
        if (user == null)
        {
            if (":console".equalsIgnoreCase(whisperTarget)||"#console".equalsIgnoreCase(whisperTarget)||"console".equalsIgnoreCase(whisperTarget))
            {
                if (context.getSender() instanceof ConsoleCommandSender)
                {
                    context.sendMessage("basics", "&eTalking to yourself?");
                    return true;
                }
                CommandSender console = context.getSender().getServer().getConsoleSender();
                console.sendMessage(_("basics", "&e%s -> You: &f%s", arr(context.getSender().getName(), message)));
                context.sendMessage("basics", "&eYou &6-> &2%s&e: &f%s", console.getName(), message);
                this.lastWhisperOfConsole = user.getName();
                user.setAttribute(basics, "lastWhisper", "#console");
                return true;
            }
            context.sendMessage("core", "&cUser %s not found!", whisperTarget);
            return true;
        }
        if (!user.isOnline())
        {
            return false;
        }
        if (context.getSender().equals(user))
        {
            context.sendMessage("basics", "&eTalking to yourself?");
            return true;
        }
        user.sendMessage("basics", "&2%s &6-> &eYou: &f%s", context.getSender().getName(), message);
        Boolean afk = user.getAttribute(basics, "afk");
        if (afk != null && afk)
        {
            context.sendMessage("basics", "&2%s &7is afk!", user.getName());
        }
        context.sendMessage(_("basics", "&eYou &6-> %&2s&e: &f%s", arr(user.getName(), message)));
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).setAttribute(basics, "lastWhisper", user.getName());
        }
        else
        {
            this.lastWhisperOfConsole = user.getName();
        }
        user.setAttribute(basics, "lastWhisper", context.getSender().getName());
        return false;
    }

    @Command(desc = "Broadcasts a message", usage = "<message>")
    public void broadcast(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasArg(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        this.um.broadcastMessage("basics", "&2[&cBroadcast&2] &e" + sb.toString());
    }

    @Command(desc = "Mutes a player", usage = "<player> [duration]", min = 1)
    public void mute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("basics", "&cUser %s not found!", context.getString(0));
            return;
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        if (bUser.muted != null && bUser.muted.getTime() < System.currentTimeMillis())
        {
            context.sendMessage("basics", "&2%s &ewas already muted!", user.getName());
        }
        Duration dura = basics.getConfiguration().defaultMuteTime;
        if (context.hasArg(1))
        {
            try
            {
                dura = new Duration(context.getStrings(1));
            }
            catch (IllegalArgumentException e)
            {
                context.sendMessage("basics", "&cInvalid duration format!");
                return;
            }
        }
        bUser.muted = new Timestamp(System.currentTimeMillis() + (dura.toMillis() == -1 ? 500 * 24 * 3600000 : dura.toMillis()));
        this.basics.getBasicUserManager().update(bUser);
        String timeString = dura.toMillis() == -1 ? "ever" : dura.format("%www %ddd %hhh %mmm %sss");
        user.sendMessage("basics", "&cYou are now muted for &6%s&c!", timeString);
        context.sendMessage("basics", "&eYou muted &2%s &eglobally for &6%s&c!", user.getName(), timeString);
    }

    @Command(desc = "Unmutes a player", usage = "<player>", min = 1, max = 1)
    public void unmute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("basics", "&cUser %s not found!", context.getString(0));
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        bUser.muted = null;
        this.basics.getBasicUserManager().update(bUser);
        context.sendMessage("basics", "&2%s &ais no longer muted!", user.getName());
    }

    @Command(desc = "Shows a random number from 0 to 100", max = 1)
    public void rand(CommandContext context)
    {
        this.um.broadcastStatus("basics", "rolled a &6%d&f!", context.getSender().getName(), (int)(Math.random() * 100));
    }
}
