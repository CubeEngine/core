package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
public class BasicsConfiguration extends Configuration
{
    @Option("spawnmob.limit")
    public int spawnmobLimit = 20;
    @Option("remove.defaultradius")
    public int removeCmdDefaultRadius = 20;
    @Comment("The world to teleport to when using /spawn"
    + "\nUse {} if you want to use the spawn of the world the player is in.")
    @Option("teleport.spawn.mainworld")
    public String spawnMainWorld = "world";
    @Comment("The seconds until a teleportrequest is automaticly denied."
    + "\nUse -1 to never automaticly deny. (Will loose information after some time when disconecting)")
    @Option("teleport.request.wait")
    public int tpRequestWait = -1;
}
