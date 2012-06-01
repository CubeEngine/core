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
    @Option("auction.default.length")
    public long default_length = 1000 * 60 * 30;//(30min) TODO format with d h m s
    @Option("auction.comission")
    public int comission = 3;//in percent
    @Option("auction.punish")
    public double punish = 0.3;
    
    public AuctionsConfiguration(YamlConfiguration config, File file)
    {
        super(config, file);
    }
}
