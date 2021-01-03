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
package org.cubeengine.libcube.service;

import org.cubeengine.libcube.util.math.shape.Shape;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.server.ServerLocation;

public interface Selector
{
    /**
     * Gets the current selection of the user
     *
     * @param user the user
     * @return the selection or null if nothing is selected
     */
    Shape getSelection(Player user);

    /**
     * Gets a projection of the current shape onto the xz-plane
     *
     * @return the projected selection
     * @param user
     */
    Shape get2DProjection(Player user);

    /**
     * Tries to get the current selection of the user as a specific selection
     *
     * @param <T>
     * @param user the user
     * @param shape the shapeType
     * @return the selection or null if the selection is not applicable
     */
    <T extends Shape> T getSelection(Player user, Class<T> shape);

    /**
     * Gets the first position
     *
     * @param user the user
     * @return the first selected position
     */
    default ServerLocation getFirstPoint(Player user)
    {
        return this.getPoint(user, 0);
    }

    /**
     * Gets the second position
     *
     * @param user the user
     * @return the second selected position
     */
    default ServerLocation getSecondPoint(Player user)
    {
        return this.getPoint(user, 1);
    }

    /**
     * Gets the n-th position in the current shape
     *
     * @param user the user
     * @param index the index
     * @return the Location
     */
    ServerLocation getPoint(Player user, int index);

    default void setFirstPoint(Player user, ServerLocation loc)
    {
        setPoint(user, 0, loc);
    }
    default void setSecondPoint(Player user, ServerLocation loc)
    {
        setPoint(user, 1, loc);
    }

    void setPoint(Player user, int index, ServerLocation loc);
}
