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
package de.cubeisland.engine.core.command.parameterized.completer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

public class WorldCompleter implements Completer
{
    private final Server server = Bukkit.getServer();

    @Override
    public List<String> complete(ParameterizedTabContext context, String token)
    {
        List<String> offers = new ArrayList<>();
        for (World world : this.server.getWorlds())
        {
            final String name = world.getName();
            if (startsWithIgnoreCase(name, token))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
