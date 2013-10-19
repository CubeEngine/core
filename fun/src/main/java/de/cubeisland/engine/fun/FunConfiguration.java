/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.fun;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Option;

public class FunConfiguration extends YamlConfiguration
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

    @Comment("Sets the maximum delay between changes of day to night and vice versa.")
    @Option("disco.delay")
    public int maxDiscoDelay = 100;

    @Comment("Sets the maximum distance between the mob and the player")
    @Option("invasion.distance")
    public int maxInvasionSpawnDistance = 10;

    @Comment("Sets the maximum height a player can jump.")
    @Option("rocket.height")
    public int maxRocketHeight = 100;

    @Comment("Sets the maximum distance of the tnt carpet")
    @Option("nuke.distance")
    public int maxNukeDistance = 50;
    @Comment("Sets the nuke radius limit")
    @Option("nuke.radius_limit")
    public int nukeRadiusLimit = 10;
    @Comment("Sets the nuke concentration limit")
    @Option("nuke.concentration_limit")
    public int nukeConcentrationLimit = 10;
    @Comment("Sets the maximum range of the explosion")
    @Option("nuke.explosion_range")
    public int nukeMaxExplosionRange = 10;
}
