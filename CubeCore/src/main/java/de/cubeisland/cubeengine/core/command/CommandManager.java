package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.bukkit.CommandWrapper;

/**
 *
 * @author Phillip Schichtel
 */
public interface CommandManager
{
    public void inject(CommandWrapper command);
    public void remove(String name);
    public void clear();
}
