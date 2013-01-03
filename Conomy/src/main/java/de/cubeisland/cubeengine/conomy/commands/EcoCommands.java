package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class EcoCommands extends ContainerCommand
{
//TODO multicurrencies
    //bank flags

    public EcoCommands(Module module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
    }
//grant

    public void give(CommandContext context)
    {
    }
//remove

    public void take(CommandContext context)
    {
    }

    public void reset(CommandContext context)
    {
    }

    public void set(CommandContext context)
    {
    }

    public void scale(CommandContext context)
    {
    }
}
