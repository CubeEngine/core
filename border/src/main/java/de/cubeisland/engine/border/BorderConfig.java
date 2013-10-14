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
package de.cubeisland.engine.border;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

public class BorderConfig extends YamlConfiguration
{
    @Option("chunk-radius")
    public int radius = 30;

    @Option("square-area")
    @Comment("Whether the radius should define a square instead of a circle around the spawn point")
    public boolean square = false;

    @Option("allow-bypass")
    @Comment("Whether players can bypass the restriction with a permission")
    public boolean allowBypass = false;

    @Option("enable-torus")
    @Comment("Experimental! The world acts as a torus. If you reach the border on the north side you'll get teleported to the south of the map")
    public boolean torusWorld = false;
}
