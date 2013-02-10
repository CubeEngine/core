package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class BasicContextFactory implements ContextFactory
{
    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        return new BasicContext(command, sender, labels, new LinkedList<String>(Arrays.asList(commandLine)));
    }
}
