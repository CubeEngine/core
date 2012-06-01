package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Faithcaio
 */
public class CoreConfiguration extends Configuration
{
    @Option("debug")
    public boolean debugMode = false;

    public CoreConfiguration(YamlConfiguration config, File file)
    {
        super(config, file);
    }
}
