package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.Bootstrapper;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phillip Schichtel
 */

public class BukkitBootstrapper extends JavaPlugin implements Bootstrapper
{
    @Override
    public void onEnable()
    {
        CubeEngine.initialize(new BukkitCore(this));
    }

    @Override
    public void onDisable()
    {
        CubeEngine.clean();
    }
}
