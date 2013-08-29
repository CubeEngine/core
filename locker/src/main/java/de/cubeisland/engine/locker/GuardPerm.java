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
package de.cubeisland.engine.locker;

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class GuardPerm extends PermissionContainer<Locker>
{

    public GuardPerm(Locker module)
    {
        super(module);
        this.bindToModule(PROTECT, ADMIN, BGUARD_COMMAND);
        this.prependModulePerm(DENY);
        this.registerAllPermissions();
    }

    private static final Permission DENY = Permission.createAbstractPermission("deny", PermDefault.FALSE);

    public static final Permission DENY_CONTAINER = DENY.createChild("container", PermDefault.FALSE);
    public static final Permission DENY_DOOR = DENY.createChild("door", PermDefault.FALSE);
    public static final Permission DENY_ENTITY = DENY.createChild("entity", PermDefault.FALSE);
    public static final Permission DENY_HANGING = DENY.createChild("hanging", PermDefault.FALSE);

    private static final Permission BGUARD_COMMAND = Permission.createAbstractPermission("command").createAbstractChild("bguard");
    public static final Permission CMD_REMOVE_OTHER = BGUARD_COMMAND.createAbstractChild("remove").createChild("other");
    public static final Permission CMD_INFO_OTHER = BGUARD_COMMAND.createAbstractChild("info").createChild("other");
    public static final Permission CMD_MODIFY_OTHER = BGUARD_COMMAND.createAbstractChild("modify").createChild("other");

    public static final Permission PROTECT = Permission.createPermission("protect"); // TODO sub perms

    public static final Permission ADMIN = Permission.createPermission("admin"); // TODO sub perms
}
