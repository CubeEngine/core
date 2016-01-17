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
package org.cubeengine.service.matcher;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.cubeengine.module.core.util.StringUtils;
import org.spongepowered.api.Game;
import org.spongepowered.api.world.World;

import static java.util.stream.Collectors.toList;

@ServiceProvider(WorldMatcher.class)
public class WorldMatcher
{
    @Inject private Game game;
    @Inject private StringMatcher stringMatcher;

    public World matchWorld(String name)
    {
        String match = stringMatcher.matchString(name, game.getServer().getWorlds().stream().map(World::getName).collect(toList()));
        if (match == null)
        {
            return null;
        }
        return game.getServer().getWorld(match).get();
    }

    /**
     * Tries to match worlds separated by ,
     * returns null at the listposition if no world was matched
     *
     * @param worldString the world in a string separated by ,
     * @return
     */
    public List<World> matchWorlds(String worldString)
    {
        List<World> worlds = new ArrayList<>();
        for (String s : StringUtils.explode(",", worldString))
        {
            worlds.add(this.matchWorld(s));
        }
        return worlds;
    }
}
