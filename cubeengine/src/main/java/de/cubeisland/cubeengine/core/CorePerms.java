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
package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.module.CoreModule;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;

public class CorePerms extends PermissionContainer<CoreModule>
{

    public CorePerms(CoreModule module)
    {
        super(module);
        this.bindToModule(COMMAND,SPAM);
        this.registerAllPermissions();
    }

    private static final Permission COMMAND = Permission.createAbstractPermission("command");
    private static final Permission CLEARPASSWORD = COMMAND.createAbstractChild("clearpassword");
    public static final Permission COMMAND_CLEARPASSWORD_ALL = CLEARPASSWORD.createChild("all");
    public static final Permission COMMAND_CLEARPASSWORD_OTHER = CLEARPASSWORD.createChild("other");

    public static final Permission COMMAND_SETPASSWORD_OTHER = COMMAND.createChild("other");
    public static final Permission COMMAND_OP_NOTIFY = COMMAND.createChild("op.notify");

    private static final Permission DEOP = COMMAND.createAbstractChild("deop");
    public static final Permission COMMAND_DEOP_NOTIFY = DEOP.createChild("notify");
    public static final Permission COMMAND_DEOP_OTHER = DEOP.createChild("other",FALSE);
    public static final Permission COMMAND_RELOAD_NOTIFY = COMMAND.createChild("reload.notify");

    public static final Permission COMMAND_VERSION_PLUGINS = COMMAND.createChild("version.plugins");

    public static final Permission SPAM = Permission.createPermission("spam");
}
