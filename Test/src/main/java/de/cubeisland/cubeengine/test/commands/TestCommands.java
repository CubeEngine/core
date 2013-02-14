package de.cubeisland.cubeengine.test.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.time.Duration;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class TestCommands
{
    @Command(desc = "Displays the colors")
    public void colortest(CommandContext context)
    {
        context.sendMessage(
            "&00 black &11 darkblue &22 darkgreen &33 darkaqua\n"
                + "&44 darkred &55 purple &66 orange &77 grey\n"
                + "&88 darkgrey &99 indigo &aa brightgreen &bb aqua\n"
                + "&cc red &dd pink &ee yellow &ff white\n"
                + "k: &kk&r &ll bold&r &mm strike&r &nn underline&r &oo italic");
    }

    @Command(desc = "Time-parsing")
    public void parsetime(CommandContext context)
    {
        LinkedList<String> list = new LinkedList<String>();
        int i = 0;
        while (context.hasArg(i))
        {
            list.add(context.getString(i));
            i++;
        }
        Duration dura = new Duration(list.toArray(new String[0]));
        context.sendMessage("ms: " + dura.toTimeUnit(TimeUnit.MILLISECONDS));
        context.sendMessage(dura.format());
    }

    @Command(desc = "A command that tests async execution.", async = true)
    public CommandResult asyncCommand(CommandContext context)
    {
        try
        {
            Thread.sleep(1000 * 5L);
        }
        catch (InterruptedException e)
        {}
        return new CommandResult() {
            @Override
            public void show(CommandSender sender)
            {
                sender.sendMessage("Delayed!");
            }
        };
    }

    @Command(desc = "This command prints out the args he gets", flags = @Flag(name = "a"), params = @Param(names = "param"))
    public void testArgs(ParameterizedContext context)
    {
        context.sendMessage("Arg dump:");
        context.sendMessage(" ");

        for (String arg : context.getArgs())
        {
            context.sendMessage("Arg: '" + arg + "'");
        }

        for (String flag : context.getFlags())
        {
            context.sendMessage("Flag: -" + flag);
        }

        for (Entry<String, Object> entry : context.getParams().entrySet())
        {
            context.sendMessage("Param: " + entry.getKey() + " => '" + entry.getValue().toString() + "'");
        }
    }

    private static final int MAX_CHAT_LINES = 100;

    @Command(names = {
    "cls", "clearscreen"
    }, desc = "Clears the chat", async = true)
    public void clearscreen(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            for (int i = 0; i < MAX_CHAT_LINES; ++i)
            {
                context.sendMessage(" ");
            }
        }
        else
        {
            context.sendMessage("&cYou better don't do this.");
        }
    }
}
