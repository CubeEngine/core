package de.cubeisland.cubeengine.log.commands;

import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

/**
 *
 * @author Anselm Brehme
 */
public class LookUp extends ContainerCommand
{
    public LookUp(Module module)
    {
        super(module, "lookup", "Searches in the database for needed informations.");
        module.registerCommand(this);
    }
}
