package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.core.persistence.filesystem.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.Option;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Faithcaio
 */
public class AuctionsConfiguration extends Configuration
{
    @Option("debug")
    public boolean debugMode = false;
    
    public AuctionsConfiguration(YamlConfiguration config, File file)
    {
        super(config, file);
    }
}
