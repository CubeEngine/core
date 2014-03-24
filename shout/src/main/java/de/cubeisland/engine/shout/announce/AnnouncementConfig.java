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

import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;

@SuppressWarnings("all")
public class AnnouncementConfig extends ReflectedYaml
{
    @Name("delay")
    public String delay = "10 minutes";

    @Name("worlds")
    public List<String> worlds = Arrays.asList("*");

    @Comment("The name that should be used in the permission. It'll end up like this: " +
                 "cubeengine.shout.announcement.permission-name")
    @Name("permission-name")
    public String permName = "*";

    @Comment("An announcement with fixed cycle will be broadcast at a fixed cycle.\n" +
                 "In opposite to it being displayed to each user after their last announcement.")
    @Name("fixed-cycle")
    public boolean fixedCycle = false;
}
