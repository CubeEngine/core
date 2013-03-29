package de.cubeisland.cubeengine.basics.command.general;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.command.CommandSender;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.time.Duration;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.storage.BasicUser;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.util.Misc.arr;

public class ChatCommands
{
    private UserManager um;
    private String lastWhisperOfConsole = null;
    private Basics module;

    public ChatCommands(Basics basics)
    {
        this.module = basics;
        this.um = basics.getCore().getUserManager();
    }

    @Command(desc = "Ignores all messages from players", min = 1, max = 1, usage = "<player>")
    // other usages: <player>[,<player>]...
    public void ignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String[] userNames = StringUtils.explode(",",context.getString(0));
            List<String> added = new ArrayList<String>();
            for (String name : userNames)
            {
                User user = this.um.findUser(name);
                if (user == null)
                {
                    context.sendMessage("basics","&cUser &2%s &cnot found!",name);
                }
                else if (!this.module.getIgnoreListManager().addIgnore(sender, user))
                {
                    context.sendMessage("basics", "&2%s&c is already on your ignore list!", user.getName());
                }
                else
                {
                    added.add(name);
                }
            }
            context.sendMessage("basics", "&aYou added &2%s&a to your ignore list!", StringUtils.implode("&f, &2",added));
            return;
        }
        int rand1 = new Random().nextInt(6)+1;
        int rand2 = new Random().nextInt(6-rand1+1)+1;
        context.sendMessage("basics", "&eIgnore (&f8+&e): %d + %d = %d -> &cfailed",rand1,rand2,rand1+rand2);
    }

    @Command(desc = "Stops ignoring all messages from a player", min = 1, max = 1, usage = "<player>")
    // other usages: <player>[,<player>]...
    public void unignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String[] userNames = StringUtils.explode(",",context.getString(0));
            List<String> added = new ArrayList<String>();
            for (String name : userNames)
            {
                User user = this.um.findUser(name);
                if (user == null)
                {
                    context.sendMessage("basics","&cUser &2%s &cnot found!",name);
                }
                else if (!this.module.getIgnoreListManager().removeIgnore(sender, user))
                {
                    context.sendMessage("basics", "&cYou haven't ignored &2%s&c!", user.getName());
                }
                else
                {
                    added.add(name);
                }
            }
            context.sendMessage("basics", "&aYou removed &2%s&a from your ignore list!", StringUtils.implode("&f, &2",added));
            return;
        }
        context.sendMessage("basics", "&cCongratulations! You are now looking at this text!");
    }

    @Command(desc = "Allows you to emote", min = 1, max = NO_MAX, usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.um.broadcastStatus(message, context.getSender().getDisplayName());
    }

    @Command(desc = "Sends a private message to someone", names = {
        "message", "msg", "tell", "pm", "m", "t", "whisper"
    }, min = 2, max = NO_MAX, usage = "<player> <message>")
    public void msg(CommandContext context)
    {
        if (!this.sendWhisperTo(context.getString(0), context.getStrings(1), context))
        {
            context.sendMessage("basics", "&cCould not find the player &2%s &cto send the message to. &eIs he offline?", context.getString(0));
        }
    }

    @Command(names = {
        "reply", "r"
    }, desc = "Replies to the last person that whispered to you.", usage = "<message>", min = 1, max = NO_MAX)
    public void reply(CommandContext context)
    {
        String lastWhisper;
        if (context.getSender() instanceof User)
        {
            lastWhisper = ((User)context.getSender()).get(BasicsAttachment.class).getLastWhisper();
        }
        else
        {
            lastWhisper = lastWhisperOfConsole;
        }
        if (lastWhisper == null)
        {
            context.sendMessage("basics", "&eNo one has send you a message that you could reply to!");
            return;
        }
        if (!this.sendWhisperTo(lastWhisper, context.getStrings(0), context))
        {
            context.sendMessage("basics", "&cCould not find the player &2%s&c to reply to. &eIs he offline?", lastWhisper);
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
                if (context.getSender() instanceof User)
                {
                    CommandSender console = context.getSender().getServer().getConsoleSender();
                    console.sendMessage(_("basics", "&e%s -> You: &f%s", arr(context.getSender().getName(), message)));
                    context.sendMessage("basics", "&eYou &6-> &2%s&e: &f%s", console.getName(), message);
                    this.lastWhisperOfConsole = context.getSender().getName();
                    ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper("#console");
                    return true;
                }
                context.sendMessage("basics","Who are you!?");
                return true;
            }
            context.sendMessage("core", "&cUser &2%s &cnot found!", whisperTarget);
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
        Boolean afk = user.get(BasicsAttachment.class).isAfk();
        if (afk != null && afk)
        {
            context.sendMessage("basics", "&2%s &7is afk!", user.getName());
        }
        context.sendMessage(_("basics", "&eYou &6-> &2%s&e: &f%s", arr(user.getName(), message)));
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper(user.getName());
        }
        else
        {
            this.lastWhisperOfConsole = user.getName();
        }
        user.get(BasicsAttachment.class).setLastWhisper(context.getSender().getName());
        return true;
    }

    @Command(desc = "Broadcasts a message", usage = "<message>", min = 1, max = NO_MAX)
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

    @Command(desc = "Mutes a player", usage = "<player> [duration]", min = 1, max = 2)
    public void mute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("basics", "&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        BasicUser bUser = this.module.getBasicUserManager().getBasicUser(user);
        if (bUser.muted != null && bUser.muted.getTime() < System.currentTimeMillis())
        {
            context.sendMessage("basics", "&2%s &ewas already muted!", user.getName());
        }
        Duration dura = module.getConfiguration().defaultMuteTime;
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
        this.module.getBasicUserManager().update(bUser);
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
            context.sendMessage("basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        BasicUser bUser = this.module.getBasicUserManager().getBasicUser(user);
        bUser.muted = null;
        this.module.getBasicUserManager().update(bUser);
        context.sendMessage("basics", "&2%s &ais no longer muted!", user.getName());
    }

    @Command(names = {"rand","roll"},desc = "Shows a random number from 0 to 100")
    public void rand(CommandContext context)
    {
        this.um.broadcastStatus("basics", "rolled a &6%d&f!", context.getSender().getName(), new Random().nextInt(100));
    }
}
