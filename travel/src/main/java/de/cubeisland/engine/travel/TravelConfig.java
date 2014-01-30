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

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;

public class TravelConfig extends YamlConfiguration
{

    public HomesSection homes;

    @Comment({"If this is set to true the commands /clearhomes and /clearwarps can only be used from the console.",
              "This will also affect \"/home admin clear\" and \"/warp admin clear\""})
    public boolean clearOnlyFromConsole = false;

    public class HomesSection implements Section
    {
        @Comment("If users should be able to have multiple homes")
        public boolean multipleHomes = true;

        @Comment("How many homes each user can have")
        public int max = 10;
    }

    public WarpsSection warps;

    public class WarpsSection implements Section
    {
        @Comment("How many warps the whole server can have")
        public int max = 100;
    }
}
