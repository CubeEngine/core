package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
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

    @Comment("An announcement with fixed cycle will be run for instance every 10 minutes.\n" +
                 "Not after the last announcements delay. It will also be shown at the same time to all users.")
    @Option("fixed-cycle")
    public boolean fixedCycle = false;
}
