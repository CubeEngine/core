package de.cubeisland.cubeengine.creeperball;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("nbt")
@DefaultConfig
public class CreeperballConfig extends Configuration
{
    @Comment("")
    @Option("team1.lights.1")
    public Location t1l1;
    @Option("team1.lights.2")
    public Location t1l2;
    @Option("team1.lights.3")
    public Location t1l3;
    @Option("team1.lights.4")
    public Location t1l4;
}
