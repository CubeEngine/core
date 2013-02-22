package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.Stack;

public interface ContextFactory
{
    ArgBounds getArgBounds();
    void setArgBounds(ArgBounds newBounds);
    CommandContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine);
}
