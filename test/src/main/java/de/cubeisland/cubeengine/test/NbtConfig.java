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
package de.cubeisland.cubeengine.test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.codec.NBTCodec;

@Codec("nbt")
public class NbtConfig extends Configuration<NBTCodec>
{
    @Option("value1")
    public Location value1 = new Location(Bukkit.getServer().getWorld("world"), 1, 2, 3, 0, 0);
    @Option("value2")
    public OfflinePlayer value2 = Bukkit.getServer().getOfflinePlayer("Faithcaio42");
    @Option("value3")
    public boolean value3 = true;
    @Option("value4.sub1")
    public int value41 = 1337;
    @Option("value4.sub2")
    public String value42 = "easy?";
}
