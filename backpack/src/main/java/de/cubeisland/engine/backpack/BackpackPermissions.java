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
package de.cubeisland.engine.backpack;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class BackpackPermissions extends PermissionContainer<Backpack>
{
    public BackpackPermissions(Backpack module)
    {
        super(module);
        this.bindToModule(OPEN_OTHER_USER, OPEN_OTHER_WORLDS);
        this.registerAllPermissions();
    }

    private static final Permission COMMAND = Permission.createAbstractPermission("command");
    private static final Permission COMMAND_OPEN = COMMAND.createAbstractChild("open");
    public static final Permission OPEN_OTHER_USER = COMMAND_OPEN.createChild("other-user");
    public static final Permission OPEN_OTHER_WORLDS = COMMAND_OPEN.createChild("other-worlds");
}
