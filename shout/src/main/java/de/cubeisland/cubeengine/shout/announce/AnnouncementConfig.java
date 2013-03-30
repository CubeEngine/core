package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

import java.util.Arrays;
import java.util.List;

@Codec("yml")
public class AnnouncementConfig extends Configuration
{
    @Option("delay")
    public String delay = "10 minutes";

    @Option("worlds")
    public List<String> worlds = Arrays.asList("*");

    @Option("permission")
    public String permNode = "*";

    @Option("group")
    public String group = "*";
}
