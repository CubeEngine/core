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
package de.cubeisland.engine.core;

import de.cubeisland.engine.core.module.CoreModule;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.permission.WildcardPermission;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;

public class CorePerms extends PermissionContainer<CoreModule>
{

    public CorePerms(CoreModule module)
    {
        super(module);
        this.bindToModule(COMMAND,SPAM);
        this.registerAllPermissions();
    }

    private static final WildcardPermission COMMAND = Permission.createWildcard("command");
    private static final WildcardPermission CLEARPASSWORD = COMMAND.childWildcard("clearpassword");
    public static final Permission COMMAND_CLEARPASSWORD_ALL = CLEARPASSWORD.child("all");
    public static final Permission COMMAND_CLEARPASSWORD_OTHER = CLEARPASSWORD.child("other");

    public static final Permission COMMAND_SETPASSWORD_OTHER = COMMAND.child("other");
    public static final Permission COMMAND_OP_NOTIFY = COMMAND.child("op.notify");

    private static final WildcardPermission DEOP = COMMAND.childWildcard("deop");
    public static final Permission COMMAND_DEOP_NOTIFY = DEOP.child("notify"); // TODO perm not used
    public static final Permission COMMAND_DEOP_OTHER = DEOP.child("other",FALSE);
    public static final Permission COMMAND_RELOAD_NOTIFY = COMMAND.child("reload.notify");

    public static final Permission COMMAND_VERSION_PLUGINS = COMMAND.child("version.plugins");

    public static final Permission SPAM = Permission.create("spam");
}
