package de.cubeisland.cubeengine.test.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;

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
}
