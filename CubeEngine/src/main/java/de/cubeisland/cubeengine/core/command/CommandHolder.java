package de.cubeisland.cubeengine.core.command;

public interface CommandHolder
{
    Class<? extends CubeCommand> getCommandType();
}
