/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.test.tests.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.MapComment;
import de.cubeisland.engine.configuration.annotations.MapComments;
import de.cubeisland.engine.configuration.annotations.Option;

/**
 * This configuration is used to test a lot of configstuff.
 */
@MapComments( {
    @MapComment(path = "regions", text = "more RandomTests:"),
    @MapComment(path = "list", text = "ListTests:"),
    @MapComment(path = "list.listinmaps.list2", text = "comment in submap"),
    @MapComment(path = "list.stringlist", text = "comment for my list :)")
})
public class TestConfig extends YamlConfiguration
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
            locs = new LinkedHashMap<>();
            locs.put("loc1", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc2", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc3", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            locs.put("loc4", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
        }
    }

    @Option("mapincollection")
    @Comment("map in collection")
    public Collection<Map<String,String>> mapinloc;
    {
        {
            Map<String,String> map = new HashMap<>();
            map.put("abc", "123");
            map.put("def", "456");
            mapinloc = new ArrayList<>();
            mapinloc.add(map);
            map = new HashMap<>();
            map.put("ghi", "789");
            map.put("jkl", "012");
            mapinloc.add(map);
        }
    }

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
}
