package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
public class AnnouncementConfig extends Configuration
{
    @Option("delay")
    public String delay = "10 minutes";
    
    @Option("world")
    public String world = "*";
    
    @Option("permission")
    public String permNode = "*";
    
    @Option("group")
    public String group = "*";
}
