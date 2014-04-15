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

import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.locker.commands.LockerCommands;

@SuppressWarnings("all")
public class LockerPerm extends PermissionContainer<Locker>
{
    public LockerPerm(Locker module, LockerCommands mainCmd)
    {
        super(module);
        CubeCommand createCmd = mainCmd.getChild("create");
        PROTECT.attach(mainCmd.getChild("info").getPermission(),
                       mainCmd.getChild("persist").getPermission(),
                       mainCmd.getChild("remove").getPermission(),
                       mainCmd.getChild("unlock").getPermission(),
                       mainCmd.getChild("modify").getPermission(),
                       mainCmd.getChild("unlock").getPermission(),
                       mainCmd.getChild("key").getPermission(),
                       mainCmd.getChild("flag").getPermission(),
                       mainCmd.getChild("give").getPermission(),
                       createCmd.getChild("private").getPermission(),
                       createCmd.getChild("public").getPermission(),
                       createCmd.getChild("donation").getPermission(),
                       createCmd.getChild("free").getPermission(),
                       createCmd.getChild("password").getPermission(),
                       createCmd.getChild("guarded").getPermission(),
                       CMD_INFO_SHOW_OWNER);
        MODERATOR.attach(PROTECT, SHOW_OWNER, CMD_INFO_OTHER, ACCESS_OTHER, CMD_REMOVE_OTHER);
        CubeCommand adminCmd = mainCmd.getChild("admin");
        ADMIN.attach(BREAK_OTHER, EXPAND_OTHER, CMD_REMOVE_OTHER, CMD_KEY_OTHER, CMD_MODIFY_OTHER, CMD_GIVE_OTHER, EXPAND_OTHER,
                     adminCmd.getChild("view").getPermission(),
                     adminCmd.getChild("remove").getPermission(),
                     adminCmd.getChild("tp").getPermission(),
                     adminCmd.getChild("purge").getPermission(),
            //         adminCmd.getChild("cleanup").getPermission(),
              //       adminCmd.getChild("list").getPermission(),
            MODERATOR);
        this.registerAllPermissions();
    }

    private final Permission DENY = getBasePerm().childWildcard("deny", PermDefault.FALSE);

    public final Permission DENY_CONTAINER = DENY.child("container", PermDefault.FALSE);
    public final Permission DENY_DOOR = DENY.child("door", PermDefault.FALSE);
    public final Permission DENY_ENTITY = DENY.child("entity", PermDefault.FALSE);
    public final Permission DENY_HANGING = DENY.child("hanging", PermDefault.FALSE);

    public final Permission SHOW_OWNER = getBasePerm().child("show-owner");
    public final Permission BREAK_OTHER = getBasePerm().child("break-other");
    public final Permission ACCESS_OTHER = getBasePerm().child("access-other");
    public final Permission EXPAND_OTHER = getBasePerm().child("break-other");

    public final Permission PREVENT_NOTIFY = getBasePerm().child("prevent-notify");

    private final Permission LOCKER_COMMAND = getBasePerm().childWildcard("command").childWildcard("locker");

    public final Permission CMD_REMOVE_OTHER = LOCKER_COMMAND.childWildcard("remove").child("other");
    public final Permission CMD_KEY_OTHER = LOCKER_COMMAND.childWildcard("key").child("other");
    public final Permission CMD_MODIFY_OTHER = LOCKER_COMMAND.childWildcard("modify").child("other");
    public final Permission CMD_GIVE_OTHER = LOCKER_COMMAND.childWildcard("give").child("other");

    private final Permission CMD_INFO = LOCKER_COMMAND.childWildcard("info");

    public final Permission CMD_INFO_OTHER = CMD_INFO.child("other");
    public final Permission CMD_INFO_SHOW_OWNER = CMD_INFO.child("show-owner");

    public final Permission PROTECT = getBasePerm().child("protect");
    public final Permission ADMIN = getBasePerm().child("admin");
    public final Permission MODERATOR = getBasePerm().child("moderator");


}
