package de.cubeisland.cubeengine.core.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author Phillip Schichtel
 */
public final class HelpContext extends BasicContext
{
    public HelpContext(CommandContext ctx)
    {
        super(ctx.getCommand(), ctx.getSender(), ctx.getLabels(), ctx.getArgs());
    }

    public HelpContext(CubeCommand command, CommandSender sender, Stack<String> labels, String[] args)
    {
        super(command, sender, labels, new LinkedList<String>(Arrays.asList(args)));
    }
}
