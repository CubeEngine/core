package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;

@Codec("yml")
@Revision(1)
public class ShoutConfiguration extends Configuration
{
    @Option("initial-delay")
    @Comment("The delay after a player joins before he receives his first message")
    public int initDelay = 20;

    @Override
    public String[] head()
    {
        return new String[] {
            "The global config for all announcements.",
            "All times are in millisecounds"
        };
    }
}