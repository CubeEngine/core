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
package de.cubeisland.engine.core.module.service.selector;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.core.util.math.shape.Shape;

public class SelectorAttachment extends UserAttachment
{
    private enum Mode
    {
        CUBOID(2);
        private int initialSize;

        Mode(int initialSize)
        {
            this.initialSize = initialSize;
        }

        public int initialSize()
        {
            return this.initialSize;
        }
    }

    private Mode mode = Mode.CUBOID;

    private World lastPointWorld;

    private List<Location> points = new ArrayList<>(mode.initialSize());

    public void setPoint(int index, Location location)
    {
        this.points.set(index, location);
        this.lastPointWorld = location.getWorld();
    }

    public int addPoint(Location location)
    {
        this.points.add(location);
        this.lastPointWorld = location.getWorld();
        return this.points.size();
    }

    public Location getPoint(int index)
    {
        return this.points.get(index);
    }

    public Shape getSelection()
    {
        for (Location point : this.points)
        {
            if (point == null) // missing point
            {
                return null;
            }
            if (lastPointWorld != point.getWorld()) // points are in different worlds
            {
                return null;
            }
        }
        return null; // TODO !!!!
    }
}
