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
package de.cubeisland.engine.kits;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

@SuppressWarnings("all")
public class KitsPerm extends PermissionContainer<Kits>
{
    public KitsPerm(Kits module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private final Permission COMMAND = getBasePerm().childWildcard("command");
    public final Permission KITS = getBasePerm().childWildcard("kits");
    public final Permission COMMAND_KIT_GIVE_FORCE = COMMAND.child("kit.give.force");
}
