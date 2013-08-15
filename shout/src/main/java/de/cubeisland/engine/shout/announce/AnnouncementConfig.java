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
package de.cubeisland.engine.shout.announce;

import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

@Codec("yml")
public class AnnouncementConfig extends Configuration
{
    @Option("delay")
    public String delay = "10 minutes";

    @Option("worlds")
    public List<String> worlds = Arrays.asList("*");

    @Option("permission")
    public String permNode = "*";

    @Comment("An announcement with fixed cycle will be broadcast at a fixed cycle.\n" +
                 "In opposite to it being displayed to each user after their last announcement.")
    @Option("fixed-cycle")
    public boolean fixedCycle = false;
}
