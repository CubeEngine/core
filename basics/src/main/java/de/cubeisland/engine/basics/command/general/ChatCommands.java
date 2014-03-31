/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.general;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.StringNode;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.TimeUtil;
import de.cubeisland.engine.core.util.converter.DurationConverter;
import de.cubeisland.engine.core.util.formatter.MessageType;
import org.joda.time.Duration;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class ChatCommands
{
    private final DurationConverter converter = new DurationConverter();
    private final UserManager um;
    private final Basics module;

    private String lastWhisperOfConsole = null;

    public ChatCommands(Basics basics)
    {
        this.module = basics;
        this.um = basics.getCore().getUserManager();
    }



    @Command(desc = "Sends a private message to someone",
             names = {"tell", "message", "msg", "pm", "m", "t", "whisper", "w"},
             usage = "<player> <message>",
             min = 2, max = NO_MAX)
    public void msg(CommandContext context)
    {
        if (!this.sendWhisperTo(context.getString(0), context.getStrings(1), context))
        {
            context.sendTranslated(MessageType.NEGATIVE, "Could not find the player {user} to send the message to. Is the player offline?", context.getString(0));
        }
    }

    @Command(names = {"reply", "r"}, usage = "<message>",
             desc = "Replies to the last person that whispered to you.",
             min = 1, max = NO_MAX)
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
            context.sendTranslated(MessageType.NEUTRAL, "No one has sent you a message that you could reply to!");
            return;
        }
        if (!this.sendWhisperTo(lastWhisper, context.getStrings(0), context))
        {
            context.sendTranslated(MessageType.NEGATIVE, "Could not find the player {user} to reply to. Is the player offline?", lastWhisper);
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
                    context.sendTranslated(MessageType.NEUTRAL, "Talking to yourself?");
                    return true;
                }
                if (context.getSender() instanceof User)
                {
                    ConsoleCommandSender console = context.getCore().getCommandManager().getConsoleSender();
                    console.sendTranslated(MessageType.NEUTRAL, "{sender} -> {text:You}: {message:color=WHITE}", context.getSender(), message);
                    context.sendTranslated(MessageType.NEUTRAL, "{text:You} -> {user}: {message:color=WHITE}", console.getName(), message);
                    this.lastWhisperOfConsole = context.getSender().getName();
                    ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper("#console");
                    return true;
                }
                context.sendTranslated(MessageType.NONE, "Who are you!?");
                return true;
            }
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", whisperTarget);
            return true;
        }
        if (!user.isOnline())
        {
            return false;
        }
        if (context.getSender().equals(user))
        {
            context.sendTranslated(MessageType.NEUTRAL, "Talking to yourself?");
            return true;
        }
        user.sendTranslated(MessageType.NONE, "{sender} -> {text:You}: {message:color=WHITE}", context.getSender().getName(), message);
        if (user.get(BasicsAttachment.class).isAfk())
        {
            context.sendTranslated(MessageType.NEUTRAL, "{user} is afk!", user.getName());
        }
        context.sendTranslated(MessageType.NEUTRAL, "{text:You} -> {user}: {message:color=WHITE}", user.getName(), message);
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
        this.um.broadcastMessage(MessageType.NEUTRAL, "[{text:Broadcast}] {}", sb.toString());
    }

    @Command(desc = "Mutes a player", usage = "<player> [duration]", min = 1, max = 2)
    public void mute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        if (basicsUserEntity.getMuted() != null && basicsUserEntity.getMuted().getTime() < System.currentTimeMillis())
        {
            context.sendTranslated(MessageType.NEUTRAL, "{user} was already muted!", user.getName());
        }
        Duration dura = module.getConfiguration().commands.defaultMuteTime;
        if (context.hasArg(1))
        {
            try
            {
                dura = converter.fromNode(StringNode.of(context.getString(1)), null);
            }
            catch (ConversionException e)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Invalid duration format!");
                return;
            }
        }
        basicsUserEntity.setMuted(new Timestamp(System.currentTimeMillis() +
            (dura.getMillis() == 0 ? TimeUnit.DAYS.toMillis(9001) : dura.getMillis())));
        basicsUserEntity.update();
        String timeString = dura.getMillis() == 0 ? user.getTranslation(MessageType.NONE, "ever") : TimeUtil.format(user.getLocale(), dura.getMillis());
        user.sendTranslated(MessageType.NEGATIVE, "You are now muted for {input#amount}!", timeString);
        context.sendTranslated(MessageType.NEUTRAL, "You muted {user} globally for {input#amount}!", user.getName(), timeString);
    }

    @Command(desc = "Unmutes a player", usage = "<player>", min = 1, max = 1)
    public void unmute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        basicsUserEntity.setMuted(null);
        basicsUserEntity.update();
        context.sendTranslated(MessageType.POSITIVE, "{user} is no longer muted!", user.getName());
    }

    @Command(names = {"rand","roll"},desc = "Shows a random number from 0 to 100")
    public void rand(CommandContext context)
    {
        this.um.broadcastStatus(ChatFormat.YELLOW,"rolled a {integer}!", context.getSender(), new Random().nextInt(100));
    }

    @Command(desc = "Displays the colors")
    public void chatcolors(CommandContext context)
    {
        context.sendTranslated(MessageType.POSITIVE, "The following chat codes are available:");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ChatFormat chatFormat : ChatFormat.values())
        {
            if (i++ % 3 == 0)
            {
                builder.append("\n");
            }
            builder.append(" ").append(chatFormat.getChar()).append(" ").append(chatFormat.toString()).append(chatFormat.name()).append(ChatFormat.RESET);
        }
        context.sendMessage(builder.toString());
        context.sendTranslated(MessageType.POSITIVE, "To use these type {text:&} followed by the code above");
    }
}
