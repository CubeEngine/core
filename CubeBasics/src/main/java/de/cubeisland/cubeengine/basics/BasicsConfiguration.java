package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
public class BasicsConfiguration extends Configuration
{
    @Option("commands.spawnmob-limit")
    public int spawnmobLimit = 20;
    @Option("commands.remove-defaultradius")
    public int removeCmdDefaultRadius = 20;
    @Comment("The world to teleport to when using /spawn"
    + "\nUse {} if you want to use the spawn of the world the player is in.")
    @Option("commands.spawn-mainworld")
    public String spawnMainWorld = "world";
    @Comment("The seconds until a teleportrequest is automaticly denied."
    + "\nUse -1 to never automaticly deny. (Will loose information after some time when disconecting)")
    @Option("commands.teleport-request-wait")
    public int tpRequestWait = -1;
    @Comment("This message will be displayed to everyone with the permission on joining!")
    @Option("commands.motd")
    public String motd = "Welcome on our server. Have fun!";
    @Option("commands.near-defaultradius")
    public int nearDefaultRadius = 20;
}
