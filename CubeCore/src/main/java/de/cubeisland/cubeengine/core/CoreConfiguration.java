package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.SectionComment;
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
    
    @SectionComment(path="regions",text="more RandomTests:")
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
    @Comment("This is a random Comment with more than one line\n2nd line incoming\n3rd line has more nuts than snickers")
    public HashMap<String, Integer> max_region_count_per_player = new HashMap<String, Integer>()
    {
        
        {
            put("default", 7);
        }
    };
    @SectionComment(path="list",text="ListTests:")
    @Option("list.stringlist")
    public List<String> stringlist = new ArrayList<String>()
    {
        {
            add("quark");
            add("kekse");
        }
    };
    @Option(value="list.playerlist",genericType=OfflinePlayer.class)
    public List<OfflinePlayer> playerlist = new ArrayList<OfflinePlayer>()
    {
        {
            add(CubeCore.getInstance().getServer().getOfflinePlayer("Faithcaio"));
            add(CubeCore.getInstance().getServer().getOfflinePlayer("KekseSpieler"));
        }
    };
    
    @Option(value="list.shortlist",genericType=Short.class)
    public List<Short> shortlist = new ArrayList<Short>()
    {
        {
            short s = 123;
            add(s);
            s = 124;
            add(s);
        }
    };
    
    @Option(value="list.listinmaps",genericType=Integer.class)
    @Comment("list in maps ftw")
    @SectionComment(path="list.listinmaps.list1",text="comment in submap")
    public HashMap<String, List<Integer>> pointlessmap = new HashMap<String, List<Integer>>()
    {
        {
            List<Integer> list1 = new ArrayList<Integer>();
            list1.add(123);
            list1.add(312);
            List<Integer> list2 = new ArrayList<Integer>();
            list2.add(124124);
            list2.add(414141);
            put("list1", list1);
            put("list2", list2);
        }
    };
}
