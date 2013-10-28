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
import javax.persistence.Transient;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.CubeEngine;

/**
 * This configuration is used to test a lot of configstuff.
 */
public class TestConfig extends YamlConfiguration
{
    @Transient
    private final Server server = ((Plugin)CubeEngine.getCore()).getServer();

    @Comment("First Comment! [report here]")
    @Name("subsection.using.annotation.bool")
    public boolean subsec1 = true;
    @Comment("Comment with more than one line\n2nd line incoming\n3rd line has more nuts than snickers")
    @Name("subsection.using.annotation.int")
    public int subsec2 = 123456;
    @Name("subsection.using.annotation.string")
    public String subsec3 = "abcdefg";

    @Comment("Comment on Section")
    @Name("subsection.using.section")
    public SubSection subsection = new SubSection();

    public class SubSection implements Section
    {
        @Comment("Comment on Field in Section")
        public boolean bool = true;
        public int integer = 123456;
        public String string = "abcdefg";
    }

    @Comment("LocationTest")
    public Location location = new Location(server.getWorld("world"), 1, 2, 3, 0, 0);

    @Comment("PlayerTest")
    public OfflinePlayer offlinePlayer = server.getOfflinePlayer("TestPlayer123");

    @Comment("Testing Collections & Arrays")
    public CollectionsStuff collections = new CollectionsStuff();

    public class CollectionsStuff implements Section
    {
        @Transient
        private final Server server = ((Plugin)CubeEngine.getCore()).getServer();

        public String[] stringarray =
            {
                "text1", "text2", "text3"
            };

        public OfflinePlayer[] playerarray =
            {
                server.getOfflinePlayer("TestPlayer"),
                server.getOfflinePlayer("OPHacker")
            };
        public Collection<String> stringlist = new LinkedList<String>()
        {

            {
                add("quark");
                add("kekse");
            }
        };
        public Collection<OfflinePlayer> playerlist = new LinkedList<OfflinePlayer>()
        {

            {
                add(server.getOfflinePlayer("TestPlayer"));
                add(server.getOfflinePlayer("Cookies"));
            }
        };
        public Collection<Short> shortlist = new LinkedList<Short>()
        {

            {
                short s = 123;
                add(s);
                s = 124;
                add(s);
            }
        };

        @Comment("map in collection")
        public Collection<Map<String,String>> mapincol;
        {
            {
                Map<String,String> map = new HashMap<>();
                map.put("abc", "123");
                map.put("def", "456");
                mapincol = new ArrayList<>();
                mapincol.add(map);
                map = new HashMap<>();
                map.put("ghi", "789");
                map.put("jkl", "012");
                mapincol.add(map);
            }
        }
    }

    @Comment("Testing Maps")
    public MapStuffs maps = new MapStuffs();

    public class MapStuffs implements Section
    {
        @Transient
        private final Server server = ((Plugin)CubeEngine.getCore()).getServer();

        public HashMap<String, Integer> hmsi = new HashMap<String, Integer>()
        {
            {
                put("default", 7);
            }
        };

        @Comment("Locations in Maps")
        public LinkedHashMap<String, Location> locs;
        {
            {
                locs = new LinkedHashMap<>();
                locs.put("loc1", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
                locs.put("loc2", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
                locs.put("loc3", new Location(server.getWorld("world"), 1, 2, 3, 0, 0));
            }
        }

        @Comment("multimapinmap")
        public LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>> mapinmapinmap = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>>()
        {

            {
                LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>> mapofmap = new LinkedHashMap<String, LinkedHashMap<String, OfflinePlayer>>()
                {

                    {
                        LinkedHashMap<String, OfflinePlayer> pmap = new LinkedHashMap<String, OfflinePlayer>()
                        {

                            {
                                this.put("inmap", Bukkit.getOfflinePlayer("String in a lot of maps"));
                            }
                        };
                        this.put("inmap", pmap);
                    }
                };
                this.put("map", mapofmap);
            }
        };
    }
}
