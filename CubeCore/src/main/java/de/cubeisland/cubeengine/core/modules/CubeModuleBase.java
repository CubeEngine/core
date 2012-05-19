package de.cubeisland.cubeengine.core.modules;

import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class CubeModuleBase extends JavaPlugin implements CubeModule
{
    private final String name;

    public CubeModuleBase(String name)
    {
        this.name = name;
    }

    public String getModuleName()
    {
        return this.name;
    }
}
