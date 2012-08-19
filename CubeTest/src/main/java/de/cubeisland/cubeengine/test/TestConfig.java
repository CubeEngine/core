package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.permission.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Anselm Brehme
 */

@MapComments({
    @MapComment(path = "regions", text = "more RandomTests:"),
    @MapComment(path = "list", text = "ListTests:"),
    @MapComment(path = "list.listinmaps.list2", text = "comment in submap"),
    @MapComment(path = "list.stringlist", text = "comment for my list :)")
})
@Codec("yml")
public class TestConfig extends Configuration
{
    private final Server server = ((Plugin)CubeEngine.getCore()).getServer();

    //TODO remove this test
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
    @Option(value = "regions.max-region-count-per-player", genericType = Integer.class)
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
    @Option(value = "arrays.stringtest", genericType = String.class)
    public String[] stringarray =
    {
        "text1", "text2"
    };
    @Option(value = "arrays.playertest", genericType = OfflinePlayer.class)
    public OfflinePlayer[] playerarray =
    {
        server.getOfflinePlayer("Anselm Brehme"),
        server.getOfflinePlayer("Niemand")
    };
    @Option("list.stringlist")
    public List<String> stringlist = new ArrayList<String>()
    {
        {
            add("quark");
            add("kekse");
        }
    };
    @Option(value = "list.playerlist", genericType = OfflinePlayer.class)
    public List<OfflinePlayer> playerlist = new ArrayList<OfflinePlayer>()
    {
        
        {
            add(server.getOfflinePlayer("Anselm Brehme"));
            add(server.getOfflinePlayer("KekseSpieler"));
        }
    };
    @Option(value = "list.shortlist", genericType = Short.class)
    public List<Short> shortlist = new ArrayList<Short>()
    {
        
        {
            short s = 123;
            add(s);
            s = 124;
            add(s);
        }
    };
    @Option(value = "list.listinmaps", genericType = List.class)
    @Comment("list in maps ftw")
    public Map<String, List<Integer>> pointlessmap = new LinkedHashMap<String, List<Integer>>()
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
    
    @Option(value="locationinmap",genericType=Location.class)
    @Comment("multi location")
    public LinkedHashMap<String, Location> locs;
    {
        {
            locs = new LinkedHashMap<String, Location>();
            locs.put("loc1", new Location(server.getWorld("world"), 1, 2, 3, 0, 0) );
            locs.put("loc2", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc3", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc4", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
        }
    };
    
    @Option("role.role")
    @Comment("a single role")
    public Role role = new Role("Gast", new ArrayList<String>(), new LinkedHashMap<String, String>(), new ArrayList<String>());
    
    @Option(value="role.roles",genericType=Role.class)
    @Comment("multi roles")
    public LinkedHashMap<String, Role> roles;
    {
        {
            roles = new LinkedHashMap<String, Role>();
            roles.put("role1", new Role("guest", new ArrayList<String>(), new LinkedHashMap<String, String>(), new ArrayList<String>()));
            roles.put("role2", new Role("member", new ArrayList<String>(), new LinkedHashMap<String, String>(), new ArrayList<String>()));
            roles.put("role3", new Role("mod", new ArrayList<String>(), new LinkedHashMap<String, String>(), new ArrayList<String>()));
            roles.put("role4", new Role("admin", new ArrayList<String>(), new LinkedHashMap<String, String>(), new ArrayList<String>()));
        }
    };
}
