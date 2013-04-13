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
package de.cubeisland.cubeengine.spawn;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

public class SpawnPerms extends PermissionContainer
{
    public SpawnPerms(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }
    private static final Permission COMMAND = Permission.BASE.createAbstractChild("spawn").createAbstractChild("command");

    private static final Permission COMMAND_SPAWN = COMMAND.createAbstractChild("spawn");
    /**
     * Allows to teleport all online players to the spawn of the main world
     */
    public static final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.createChild("all");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public static final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.createChild("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public static final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.createChild("force");

}
