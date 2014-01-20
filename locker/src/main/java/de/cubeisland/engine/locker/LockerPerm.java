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

public class LockerPerm extends PermissionContainer<Locker>
{
    public LockerPerm(Locker module, LockerCommands mainCmd)
    {
        super(module);
        this.bindToModule(PROTECT, MODERATOR, ADMIN, LOCKER_COMMAND, SHOW_OWNER, BREAK_OTHER, EXPAND_OTHER, ACCESS_OTHER, PREVENT_NOTIFY);
        this.prependModulePerm(DENY);
        CubeCommand createCmd = mainCmd.getChild("create");
        PROTECT.attach(Permission.create(mainCmd.getChild("info").getPermission()),
                       Permission.create(mainCmd.getChild("persist").getPermission()),
                       Permission.create(mainCmd.getChild("remove").getPermission()),
                       Permission.create(mainCmd.getChild("unlock").getPermission()),
                       Permission.create(mainCmd.getChild("modify").getPermission()),
                       Permission.create(mainCmd.getChild("unlock").getPermission()),
                       Permission.create(mainCmd.getChild("key").getPermission()),
                       Permission.create(mainCmd.getChild("flag").getPermission()),
                       Permission.create(mainCmd.getChild("give").getPermission()),
                       Permission.create(createCmd.getChild("private").getPermission()),
                       Permission.create(createCmd.getChild("public").getPermission()),
                       Permission.create(createCmd.getChild("donation").getPermission()),
                       Permission.create(createCmd.getChild("free").getPermission()),
                       Permission.create(createCmd.getChild("password").getPermission()),
                       Permission.create(createCmd.getChild("guarded").getPermission()),
                       CMD_INFO_SHOW_OWNER);
        MODERATOR.attach(PROTECT, SHOW_OWNER, CMD_INFO_OTHER, ACCESS_OTHER, CMD_REMOVE_OTHER);
        CubeCommand adminCmd = mainCmd.getChild("admin");
        ADMIN.attach(BREAK_OTHER, EXPAND_OTHER, CMD_REMOVE_OTHER, CMD_KEY_OTHER, CMD_MODIFY_OTHER, CMD_GIVE_OTHER, EXPAND_OTHER,
                     Permission.create(adminCmd.getChild("view").getPermission()),
                     Permission.create(adminCmd.getChild("remove").getPermission()),
                     Permission.create(adminCmd.getChild("tp").getPermission()),
                     Permission.create(adminCmd.getChild("purge").getPermission()),
            //         Permission.create(adminCmd.getChild("cleanup").getPermission()),
              //       Permission.create(adminCmd.getChild("list").getPermission()),
            MODERATOR);
        this.registerAllPermissions();
    }

    private static final Permission DENY = Permission.createWildcard("deny", PermDefault.FALSE);

    public static final Permission DENY_CONTAINER = DENY.createChild("container", PermDefault.FALSE);
    public static final Permission DENY_DOOR = DENY.createChild("door", PermDefault.FALSE);
    public static final Permission DENY_ENTITY = DENY.createChild("entity", PermDefault.FALSE);
    public static final Permission DENY_HANGING = DENY.createChild("hanging", PermDefault.FALSE);

    public static final Permission SHOW_OWNER = Permission.create("show-owner");
    public static final Permission BREAK_OTHER = Permission.create("break-other");
    public static final Permission ACCESS_OTHER = Permission.create("access-other");
    public static final Permission EXPAND_OTHER = Permission.create("break-other");

    public static final Permission PREVENT_NOTIFY = Permission.create("prevent-notify");

    private static final Permission LOCKER_COMMAND = Permission.createWildcard("command").childWildcard("locker");

    public static final Permission CMD_REMOVE_OTHER = LOCKER_COMMAND.createAbstractChild("remove").createChild("other");
    public static final Permission CMD_KEY_OTHER = LOCKER_COMMAND.createAbstractChild("key").createChild("other");
    public static final Permission CMD_MODIFY_OTHER = LOCKER_COMMAND.createAbstractChild("modify").createChild("other");
    public static final Permission CMD_GIVE_OTHER = LOCKER_COMMAND.createAbstractChild("give").createChild("other");

    private static final Permission CMD_INFO = LOCKER_COMMAND.createAbstractChild("info");

    public static final Permission CMD_INFO_OTHER = CMD_INFO.createChild("other");
    public static final Permission CMD_INFO_SHOW_OWNER = CMD_INFO.createChild("show-owner");

    public static final Permission PROTECT = Permission.create("protect");
    public static final Permission ADMIN = Permission.create("admin");
    public static final Permission MODERATOR = Permission.create("moderator");


}
