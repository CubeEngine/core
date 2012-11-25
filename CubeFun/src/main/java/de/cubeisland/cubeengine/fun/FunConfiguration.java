package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class FunConfiguration extends Configuration
{
    @Comment("Sets the maximum distance of the lightning")
    @Option("lightning.distance")
    public int lightningDistance = 200;
    
    @Comment("Sets the maximum distance of the explosion")
    @Option("explosion.distance")
    public int explosionDistance = 30;
    @Comment("Sets the maximum power of the explosion")
    @Option("explosion.power")
    public int explosionPower = 20;
    
    @Comment("Sets the maximum number of thrown Objects")
    @Option("throw.number")
    public int maxThrowNumber = 50;
    @Comment("Sets the maximum delay of this command")
    @Option("throw.delay")
    public int maxThrowDelay = 30;
    @Comment("Sets the maximum number of fireballs")
    @Option("fireball.number")
    public int maxFireballNumber = 10;
    @Comment("Sets the maximum delay of this command")
    @Option("fireball.delay")
    public int maxFireballDelay = 30;
    
    
    @Comment("Sets the maximum amount of changes of day to night and vice versa.")
    @Option("disco.changes")
    public int maxDiscoChanges = 20;
    
    
    @Comment("Sets the maximum distance between the mob and the player")
    @Option("invasion.distance")
    public int maxInvasionSpawnDistance = 10;
    
    
    @Comment("Sets the maximum height a player can jump. Maximum is 100")
    @Option("rocket.height")
    public int maxRocketHeight = 100;
    
    
    @Comment("Set the maximum distance of the tnt carpet")
    @Option("nuke.distance")
    public int maxNukeDistance = 50;
    @Comment("Set the nuke radius limit")
    @Option("nuke.radius_limit")
    public int nukeRadiusLimit = 10;
    @Comment("Set the nuke concentration limit")
    @Option("nuke.concentration_limit") 
    public int nukeConcentrationLimit = 10;
    @Comment("Set the maximum range of the explosion")
    @Option("nuke.explosion_range")
    public int nukeMaxExplosionRange = 10;
}
