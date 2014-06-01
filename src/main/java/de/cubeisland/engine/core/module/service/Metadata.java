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

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;

public interface Metadata
{
    /**
     * Sets MetaData for given PlayerUUID in the current world
     *
     * @return the previous value or null
     */
    String setMetadata(User user, String key, String value);

    /**
     * Gets MetaData for given PlayerUUID and key in the current world
     *
     * @return the value or null
     */
    String getMetadata(User user, String key);

    /**
     * Sets MetaData for given PlayerUUID in the given world
     *
     * @return the previous value or null
     */
    String setMetadata(User user, World world, String key, String value);

    /**
     * Gets MetaData for given PlayerUUID and key in the given world
     *
     * @return the value or null
     */
    String getMetadata(User user, World world, String key);

    /**
     * Gets MetaData for given role and key in the given world
     * <p>pass null world for global role
     *
     * @return the value or null
     */
    String getRoleMetadata(String role, World world, String key);

    /**
     * Sets MetaData for given role and key in the given world
     * <p>pass null world for global role
     *
     * @return the previous value or null
     */
    String setRoleMetadata(String role, World world, String key, String value);

    String getName();
}
