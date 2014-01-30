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
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.StringNode;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.TimeUtil;
import de.cubeisland.engine.core.util.converter.DurationConverter;
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



    @Command(desc = "Sends a private message to someone", names = {
        "message", "msg", "tell", "pm", "m", "t", "whisper", "w"
    }, min = 2, max = NO_MAX, usage = "<player> <message>")
    public void msg(CommandContext context)
    {
        if (!this.sendWhisperTo(context.getString(0), context.getStrings(1), context))
        {
            context.sendTranslated("&cCould not find the player &2%s &cto send the message to. &eIs he offline?", context.getString(0));
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
            context.sendTranslated("&eNo one has send you a message that you could reply to!");
            return;
        }
        if (!this.sendWhisperTo(lastWhisper, context.getStrings(0), context))
        {
            context.sendTranslated("&cCould not find the player &2%s&c to reply to. &eIs he offline?", lastWhisper);
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
                    context.sendTranslated("&eTalking to yourself?");
                    return true;
                }
                if (context.getSender() instanceof User)
                {
                    ConsoleCommandSender console = context.getCore().getCommandManager().getConsoleSender();
                    console.sendTranslated("&e%s -> You: &f%s", context.getSender().getDisplayName(), message);
                    context.sendTranslated("&eYou &6-> &2%s&e: &f%s", console.getName(), message);
                    this.lastWhisperOfConsole = context.getSender().getName();
                    ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper("#console");
                    return true;
                }
                context.sendTranslated("Who are you!?");
                return true;
            }
            context.sendTranslated("&cUser &2%s &cnot found!", whisperTarget);
            return true;
        }
        if (!user.isOnline())
        {
            return false;
        }
        if (context.getSender().equals(user))
        {
            context.sendTranslated("&eTalking to yourself?");
            return true;
        }
        user.sendTranslated("&2%s &6-> &eYou: &f%s", context.getSender().getName(), message);
        if (user.get(BasicsAttachment.class).isAfk())
        {
            context.sendTranslated("&2%s &7is afk!", user.getName());
        }
        context.sendTranslated("&eYou &6-> &2%s&e: &f%s", user.getName(), message);
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
        this.um.broadcastMessage("&2[&cBroadcast&2] &e" + sb.toString());
    }

    @Command(desc = "Mutes a player", usage = "<player> [duration]", min = 1, max = 2)
    public void mute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        if (basicsUserEntity.getMuted() != null && basicsUserEntity.getMuted().getTime() < System.currentTimeMillis())
        {
            context.sendTranslated("&2%s &ewas already muted!", user.getName());
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
                context.sendTranslated("&cInvalid duration format!");
                return;
            }
        }
        basicsUserEntity.setMuted(new Timestamp(System.currentTimeMillis() +
            (dura.getMillis() == 0 ? TimeUnit.DAYS.toMillis(9001) : dura.getMillis())));
        basicsUserEntity.update();
        String timeString = dura.getMillis() == 0 ? user.translate("ever") : TimeUtil.format(user.getLocale(), dura.getMillis());
        user.sendTranslated("&cYou are now muted for &6%s&c!", timeString);
        context.sendTranslated("&eYou muted &2%s &eglobally for &6%s&c!", user.getName(), timeString);
    }

    @Command(desc = "Unmutes a player", usage = "<player>", min = 1, max = 1)
    public void unmute(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        basicsUserEntity.setMuted(null);
        basicsUserEntity.update();
        context.sendTranslated("&2%s&a is no longer muted!", user.getName());
    }

    @Command(names = {"rand","roll"},desc = "Shows a random number from 0 to 100")
    public void rand(CommandContext context)
    {
        this.um.broadcastStatus(ChatFormat.YELLOW,"rolled a &6%d&f!", context.getSender(), new Random().nextInt(100));
    }

    @Command(desc = "Displays the colors")
    public void chatcolors(CommandContext context)
    {
        context.sendMessage("&aThe following chat-codes are available:");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        String reset = ChatFormat.parseFormats("&r");
        for (ChatFormat chatFormat : ChatFormat.values())
        {
            if (i++ % 3 == 0)
            {
                builder.append("\n");
            }
            builder.append(" ").append(chatFormat.getChar()).append(" ").append(chatFormat.toString()).append(chatFormat.name()).append(reset);
        }
        context.sendMessage(builder.toString());
        context.sendTranslated("&aTo use these type &6&&a followed by the code above");

       /*context.sendMessage(
            "&00 black &11 darkblue &22 darkgreen &33 darkaqua\n"
                + "&44 darkred &55 purple &66 orange &77 grey\n"
                + "&88 darkgrey &99 indigo &aa brightgreen &bb aqua\n"
                + "&cc red &dd pink &ee yellow &ff white\n"
                + "k: &kk&r &ll bold&r &mm strike&r &nn underline&r &oo italic");*/
    }
}
