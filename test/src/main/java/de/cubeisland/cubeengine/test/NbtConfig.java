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
