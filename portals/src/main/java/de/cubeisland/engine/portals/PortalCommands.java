package de.cubeisland.engine.portals;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;

public class PortalCommands extends ContainerCommand
{
    public PortalCommands(Module module)
    {
        super(module, "portals", "The portal commands");
    }

    @Command(desc = "Creates a new Portal")
    public void create(CommandContext context)
    {

    }
}
