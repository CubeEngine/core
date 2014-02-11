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
package de.cubeisland.engine.log;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import de.cubeisland.engine.core.user.User;

public class Preview
{
    private final Queue<BlockState> states = new LinkedList<>();

    public void add(BlockState state)
    {
        states.add(state);
    }

    public void send(User user)
    {
        // test limit preview changes to 1k
        Location location = new Location(null, 0, 0, 0);
        for (int i = 0 ; i < 1000; i++)
        {
            if (states.isEmpty()) return;
            BlockState poll = states.poll();
            poll.getLocation(location);
            user.sendBlockChange(location, poll.getType(), poll.getRawData());
        }
    }
}
