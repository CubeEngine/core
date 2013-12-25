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
package de.cubeisland.engine.hide;

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class HidePerm extends PermissionContainer<Hide>
{
    public HidePerm(Hide module)
    {
        super(module);

        this.bindToModule(AUTO_HIDE, AUTO_SEEHIDDENS, INTERACT, DROP, CHAT);

        this.registerAllPermissions();
    }

    private static final Permission AUTO = Permission.createAbstractPermission("auto");
    public static final Permission AUTO_HIDE = AUTO.createChild("hide", PermDefault.FALSE);
    public static final Permission AUTO_SEEHIDDENS = AUTO.createChild("seehiddens", PermDefault.FALSE);

    public static final Permission INTERACT = Permission.createPermission("interact", PermDefault.FALSE);
    public static final Permission DROP = Permission.createPermission("drop", PermDefault.FALSE);
    public static final Permission CHAT = Permission.createPermission("chat", PermDefault.FALSE);
}
