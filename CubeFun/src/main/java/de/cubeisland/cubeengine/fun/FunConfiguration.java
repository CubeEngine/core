package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
public class FunConfiguration extends Configuration
{
    @Comment("Sets the maximum distance of the lightning")
    @Option("lightning.distance")
    public int lightningDistance = 200;

    @Comment("Sets the maximum number of thrown Objects")
    @Option("throw.number")
    public int maxThrowNumber    = 20;

    @Comment("Sets the maximum number of fireballs")
    @Option("fireball.number")
    public int maxFireballNumber = 10;

    @Comment("Sets the maximum height a player can jump. Maximum is 100")
    @Option("rocket.height")
    public int maxRocketHeight   = 100;

    @Comment("Set the maximum distance of the tnt carpet")
    @Option("nuke.distance")
    public int maxNukeDistance   = 50;
    @Comment("Set the nuke radius limit")
    @Option("nuke.radius_limit")
    public int nukeRadiusLimit   = 10;

    /*
    @Option("nuke.concentrationradius")
    public Map<Integer, Integer> nukeRadius = new HashMap<Integer, Integer>()
    {
        {
            put(1, 10);
            put(2, 15);
            put(3, 20);
            put(4, 23);
            put(5, 25);
        }
    };
     */
}
