package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class BasicContextFactory implements ContextFactory
{
    private final ArgBounds bounds;

    public BasicContextFactory(ArgBounds bounds)
    {
        this.bounds = bounds;
    }

    @Override
    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        if (commandLine.length < this.getArgBounds().getMin())
        {
            throw new IncorrectUsageException("You've given too few arguments.");
        }
        if (this.getArgBounds().getMax() > ArgBounds.NO_MAX && commandLine.length > this.getArgBounds().getMax())
        {
            throw new IncorrectUsageException("You've given too many arguments.");
        }
        return new BasicContext(command, sender, labels, new LinkedList<String>(Arrays.asList(commandLine)));
    }
}
