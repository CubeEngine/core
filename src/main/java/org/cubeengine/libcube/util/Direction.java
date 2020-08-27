/*
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
package org.cubeengine.libcube.util;

import net.kyori.adventure.text.Component;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.entity.living.player.Player;

public enum Direction
{
    NORTH(23),
    NORTH_EAST(68),
    EAST(113),
    SOUTH_EAST(158),
    SOUTH(203),
    SOUTH_WEST(248),
    WEST(293),
    NORTH_WEST(338);

    private final int dir;

    private Direction(int dir)
    {
        this.dir = dir;
    }

    public static Direction matchDirection(int dir)
    {
        for (Direction direction : values())
        {
            if (dir < direction.dir)
            {
                return direction;
            }
        }
        return Direction.NORTH;
    }

    public Component translated(Player sender, I18n i18n)
    {
        switch (this)
        {
            case NORTH:
                return i18n.translate(sender, "north");
            case NORTH_EAST:
                return i18n.translate(sender, "north-east");
            case EAST:
                return i18n.translate(sender, "east");
            case SOUTH_EAST:
                return i18n.translate(sender, "south-east");
            case SOUTH:
                return i18n.translate(sender, "south");
            case SOUTH_WEST:
                return i18n.translate(sender, "south-west");
            case WEST:
                return i18n.translate(sender, "west");
            case NORTH_WEST:
                return i18n.translate(sender, "north-west");
            default:
                throw new IllegalStateException();
        }
    }
}
