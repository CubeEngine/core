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
package de.cubeisland.engine.travel;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.DefaultConfig;
import de.cubeisland.engine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class TravelConfig extends Configuration
{

    @Comment("If users should be able to have multiple homes")
    @Option("homes.multiple-homes")
    public boolean multipleHomes = true;

    @Comment("How many homes each user can have")
    @Option("homes.max")
    public int maxhomes = 10;

    @Comment("How many warps the whole server can have")
    @Option("warps.max")
    public int maxwarps = 100;
}
