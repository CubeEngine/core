package de.cubeisland.cubeengine.log.action;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import de.cubeisland.cubeengine.log.Log;

public abstract class BukkitActionType extends ActionType implements Listener
{
    private final Plugin plugin;

    public BukkitActionType(Log module, Plugin plugin, int id, String name)
    {
        super(module, id, name);
        this.plugin = plugin;
    }
}
