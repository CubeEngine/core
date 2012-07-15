package de.cubeisland.cubeengine.core.abstraction;

import java.io.File;

/**
 *
 * @author CodeInfection
 */
public interface Plugin
{
    public String getName();

    public String getVersion();

    public PluginDescription getDescription();

    public File getDataFolder();

    public void enable();

    public void disable();

    public void reload();

    public Configuration getConfiguration();

    public boolean isEnabled();
}
