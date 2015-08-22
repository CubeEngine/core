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
package org.cubeengine.service.command.completer;

import java.util.ArrayList;
import java.util.List;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.completer.Completer;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.World;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;

public class WorldCompleter implements Completer
{
    private Server server;

    public WorldCompleter(Server server)
    {
        this.server = server;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        List<String> offers = new ArrayList<>();
        for (World world : server.getWorlds())
        {
            final String name = world.getName();
            if (startsWithIgnoreCase(name, invocation.currentToken()))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
