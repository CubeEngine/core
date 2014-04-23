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

import de.cubeisland.engine.reflect.Section;
import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;

@SuppressWarnings("all")
public class FunConfiguration extends ReflectedYaml
{
    public CommandSection command;

    public class CommandSection implements Section
    {
        public LightningSection lightning;
        public ExplosionSection explosion;
        public BurnSection burn;
        @Name("throw")
        public ThrowSection throwSection;
        public DiscoSection disco;
        public InvasionSection invasion;
        public RocketSection rocket;
        public NukeSection nuke;

        public class LightningSection implements Section
        {
            @Comment("Sets the (maximum) distance of the lightning")
            public int distance = 200;
        }

        public class ExplosionSection implements Section
        {
            @Comment("Sets the (maximum) distance of the explosion")
            public int distance = 30;

            @Comment("Sets the maximum power of the explosion")
            public int power = 20;
        }

        public class ThrowSection implements Section
        {
            @Comment("Sets the maximum number of thrown Objects")
            @Name("max.amount")
            public int maxAmount = 50;

            @Comment("Sets the maximum delay of this command")
            @Name("max.delay")
            public int maxDelay = 30;
        }

        public class BurnSection implements Section
        {
            @Comment("Sets the maximum time in seconds for how long a player burns")
            @Name("max.time")
            public int maxTime = 30;
        }

        public class DiscoSection implements Section
        {
            @Comment("Sets the minimum delay between changes of day to night and vice versa")
            @Name("delay.min")
            public int minDelay = 1;

            @Comment("Sets the maximum delay between changes of day to night and vice versa")
            @Name("delay.max")
            public int maxDelay = 100;

            @Comment("Sets the default delay of the disco command. Has to be between the max and the min value")
            @Name("delay.default")
            public int defaultDelay = 10;
        }

        public class InvasionSection implements Section
        {
            @Comment("Sets the (maximum) distance between the mob and the player")
            @Name("distance")
            public int distance = 10;
        }

        public class RocketSection implements Section
        {
            @Comment("Sets the maximum height a player can jump")
            @Name("max.height")
            public int maxHeight = 100;
        }

        public class NukeSection implements Section
        {
            @Comment("Sets the (maximum) distance of the tnt carpet")
            public int distance = 50;

            @Comment("Sets the maximum amount of tnt blocks which are used for the tnt carpet")
            @Name("max.tnt_amount")
            public int maxTNTAmount = 750;

            @Comment("Sets the maximum range of the explosion")
            @Name("max.explosion_range")
            public int maxExplosionRange = 10;
        }
    }
}
