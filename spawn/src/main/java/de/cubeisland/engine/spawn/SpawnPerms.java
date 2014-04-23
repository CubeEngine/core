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
package de.cubeisland.engine.spawn;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

@SuppressWarnings("all")
public class SpawnPerms extends PermissionContainer<Spawn>
{
    public SpawnPerms(Spawn module)
    {
        super(module);
        this.registerAllPermissions();
    }
    private final Permission COMMAND_SPAWN = getBasePerm().childWildcard("command").childWildcard("spawn");
    /**
     * Allows to teleport all online players to the spawn of the main world
     */
    public final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.child("all");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.child("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.child("force");

}
