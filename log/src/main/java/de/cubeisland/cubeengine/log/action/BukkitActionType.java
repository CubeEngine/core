package de.cubeisland.cubeengine.log.action;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import de.cubeisland.cubeengine.log.Log;

public abstract class BukkitActionType extends ActionType implements Listener
{
    private final Plugin plugin;

    public BukkitActionType(Log module, Plugin plugin, String name, boolean canRollback, Category... types)
    {
        super(module, name, canRollback);
        this.plugin = plugin;
    }
}
