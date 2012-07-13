package de.cubeisland.cubeengine.core;

import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Faithcaio
 */

public class BukkitCore extends JavaPlugin
{
   CubeCore core;

    @Override
    public void onEnable()
    {
        this.core = new CubeCore(this);
        this.core.onEnable();
    }

    @Override
    public void onDisable()
    {
        this.core.onDisable();
        core = null;
    }
}
