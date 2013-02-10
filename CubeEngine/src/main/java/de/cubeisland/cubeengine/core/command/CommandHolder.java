package de.cubeisland.cubeengine.core.command;

public interface CommandHolder
{
    public Class<? extends CubeCommand> getCommandType();
}
