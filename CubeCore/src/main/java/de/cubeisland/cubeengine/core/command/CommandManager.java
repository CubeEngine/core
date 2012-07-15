package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.bukkit.BukkitCommandWrapper;

/**
 *
 * @author Phillip Schichtel
 */
public interface CommandManager
{
    public void inject(BukkitCommandWrapper command);
    public void remove(String name);
    public void clear();
}
