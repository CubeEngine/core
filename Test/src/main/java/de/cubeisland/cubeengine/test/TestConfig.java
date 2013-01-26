package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.*;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * This configuration is used to test a lot of configstuff.
 */
@MapComments( {
    @MapComment(path = "regions", text = "more RandomTests:"),
    @MapComment(path = "list", text = "ListTests:"),
    @MapComment(path = "list.listinmaps.list2", text = "comment in submap"),
    @MapComment(path = "list.stringlist", text = "comment for my list :)")
})
@Codec("yml")
@DefaultConfig()
public class TestConfig extends Configuration
{
    private final Server server = ((Plugin)CubeEngine.getCore()).getServer();
    @Option("location")
    @Comment("LocationTest")
    public Location location = new Location(server.getWorld("world"), 1, 2, 3, 0, 0);
    @Option("offlineplayer")
    @Comment("PlayerTest")
    public OfflinePlayer player = server.getOfflinePlayer("Anselm Brehme");
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
    @Option("regions.the42")
    public Integer the42 = 42;
    @Option("regions.the21")
    public int the21 = 21;
    @Option("arrays.stringtest")
    public String[] stringarray =
    {
        "text1", "text2"
    };
    @Option("arrays.playertest")
    public OfflinePlayer[] playerarray =
    {
        server.getOfflinePlayer("Anselm Brehme"),
        server.getOfflinePlayer("Niemand")
    };
    @Option("list.stringlist")
    public Collection<String> stringlist = new LinkedList<String>()
    {

        {
            add("quark");
            add("kekse");
        }
    };
    @Option("list.playerlist")
    public Collection<OfflinePlayer> playerlist = new LinkedList<OfflinePlayer>()
    {

        {
            add(server.getOfflinePlayer("Anselm Brehme"));
            add(server.getOfflinePlayer("KekseSpieler"));
        }
    };
    @Option("list.shortlist")
    public Collection<Short> shortlist = new LinkedList<Short>()
    {

        {
            short s = 123;
            add(s);
            s = 124;
            add(s);
        }
    };
    @Option("locationinmap")
    @Comment("multi location")
    public LinkedHashMap<String, Location> locs;

    {
        {
            locs = new LinkedHashMap<String, Location>();
            locs.put("loc1", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc2", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc3", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc4", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
        }
    };

    @Option("mapinmapinmap")
    @Comment("multimapinmap")
    public LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>> thingy = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>>()
    {

        {
            LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>> intmap = new LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>()
            {

                {
                    LinkedHashMap<String, OfflinePlayer> pmap = new LinkedHashMap<String, OfflinePlayer>()
                    {

                        {
                            this.put("theplayer", player);
                        }
                    };
                    this.put("iOncewasAnInt", pmap);
                }
            };
            this.put("keks", intmap);
        }
    };
    @Option("subconfig")
    public TestSubConfig subConfig = new TestSubConfig();

    public class TestSubConfig extends Configuration
    {
        @Option("sub.int")
        @Comment("SubMapComment1")
        public int subInt = 1;
        @Option("sub.doub")
        public double subdoub = 2.3;
        @Option("sub.string")
        public String substri = "nothin";
        @Option("subsubconfig")
        public TestSubSubConfig suboptimaleConfig = new TestSubSubConfig();

        public class TestSubSubConfig extends Configuration
        {
            @Comment("SubMapComment2")
            @Option("sub.int")
            public int subInt = 1;
            @Option("sub.doub")
            public double subdoub = 2.3;
            @Option("sub.string")
            public String substri = "something";
            @Option("offlineplayer")
            public String offlineplayer = "noplayer";
        }
    }
}
