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
package de.cubeisland.engine.core.module.service;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.shape.Shape;

public interface Selector
{
    /**
     * Gets the current selection of the user
     *
     * @param user the user
     * @return the selection or null if nothing is selected
     */
    public Shape getSelection(User user);

    /**
     * Gets a projection of the current shape onto the xz-plane
     *
     * @return the projected selection
     */
    public Shape get2DProjection(User user);

    /**
     * Tries to get the current selection of the user as a specific selection
     *
     * @param user the user
     * @param shape the shapeType
     * @param <T>
     * @return the selection or null if the selection is not applicable
     */
    public <T extends Shape> T getSelection(User user, Class<T> shape);

    /**
     * Gets the first position
     *
     * @param user the user
     * @return the first selected position
     */
    public Location getFirstPoint(User user);

    /**
     * Gets the second position
     *
     * @param user the user
     * @return the second selected position
     */
    public Location getSecondPoint(User user);

    /**
     * Gets the n-th position in the current shape
     *
     * @param user the user
     * @param index the index
     * @return the Location
     */
    public Location getPoint(User user, int index);
}
