package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.SComment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 */
public class CoreConfiguration extends Configuration
{
    @Option("debug")
    @Comment("If enabled shows debug-messages")
    public boolean debugMode = false;
    
    @Option("defaultLanguage")
    @Comment("Sets the language to choose by default")
    public String defaultLanguage = "en_US";

    //TODO remove this test
    @Option("location")
    @Comment("LocationTest")
    public Location location = new Location(CubeCore.getInstance().getServer().getWorld("world"), 1, 2, 3, 0, 0);
    @Option("offlineplayer")
    @Comment("PlayerTest")
    public OfflinePlayer player = CubeCore.getInstance().getServer().getOfflinePlayer("Faithcaio");
    
    @SComment(path="regions",text="more RandomTests:")
    @Option("regions.use-scheduler")
    public boolean use_scheduler = true;
    @Option("regions.sql.use")
    public boolean sql_use = false;
    @Option("regions.sql.dsn")
    public String sql_dsn = "jdbc:mysql://localhost/worldguard";
    @Comment("RandomComment")
    @Option("regions.sql.username")
    public String sql_username = "worldguard";
    @Option("regions.sql.password")
    public String sql_password = "worldguard";
    @Option("regions.max-region-count-per-player")
    public HashMap<String, Integer> max_region_count_per_player = new HashMap<String, Integer>()
    {
        
        {
            put("default", 7);
        }
    };
    @Option("fire.disable-lava-fire-spread")
    public boolean disable_lava_fire_spread = false;
    @Option("fire.disable-all-fire-spread")
    public boolean disable_all_fire_spread = false;
    @Option("fire.disable-fire-spread-blocks")
    public List<String> disable_fire_spread_blocks = new ArrayList<String>();
    @Option("fire.lava-spread-blocks")
    public List<String> lava_spread_blocks = new ArrayList<String>();
}
