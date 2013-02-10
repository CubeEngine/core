package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.module.Module;
import java.util.List;

public interface CommandFactory<T extends CubeCommand>
{
    Class<T> getCommandType();
    List<T> parseCommands(Module module, Object holder);
}
