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
package de.cubeisland.engine.module.core.sponge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import de.cubeisland.engine.module.core.Core;
import de.cubeisland.engine.module.core.logging.LoggingUtil;
import de.cubeisland.engine.module.core.module.Module;
import de.cubeisland.engine.module.core.permission.NotifyPermissionRegistrationCompletedEvent;
import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.module.core.permission.PermissionManager;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

public class BukkitPermissionManager implements PermissionManager
{
    private final Map<String, Permission> permissions = new HashMap<>();
    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Log logger;

    @SuppressWarnings("unchecked")
    public BukkitPermissionManager(SpongeCore core)
    {
        this.logger = core.getLogFactory().getLog(Core.class, "Permissions");
        this.logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "Permissions"),
                                                  LoggingUtil.getFileFormat(false, false),
                                                  false, LoggingUtil.getCycler(),
                                                  core.getTaskManager().getThreadFactory()));
        this.registerPermission(core.getModuleManager().getCoreModule(), Permission.BASE);
    }

    private Set<String> getPermissions(Module module)
    {
        Set<String> perms = this.modulePermissionMap.get(module);
        if (perms == null)
        {
            this.modulePermissionMap.put(module, perms = new HashSet<>(1));
        }
        return perms;
    }

    @Override
    public void registerPermission(Module module, Permission permission)
    {
        String fullName = permission.getFullName();
        if (permissions.containsKey(fullName))
        {
            return; // already registered
            // TODO check for updates?
        }
        permissions.put(fullName, permission);
        Set<String> byModule = getPermissions(module);
        byModule.add(fullName);

        // Register all Parents
        for (Permission parentPerm : permission.getParents())
        {
            this.registerPermission(module, parentPerm);
        }
    }

    @Override
    public void notifyPermissionRegistrationCompleted(Module module, Permission... permissions)
    {
        module.getCore().getEventManager().fireEvent(new NotifyPermissionRegistrationCompletedEvent(module, permissions));
    }

    @Override
    public void removePermission(Module module, String perm)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(perm, "The permission must not be null!");
        expect(!perm.equals(Permission.BASE.getName() + ".*"), "The CubeEngine wildcard permission must not be unregistered!");

        Set<String> perms = this.modulePermissionMap.get(module);
        if (perms != null)
        {
            perms.remove(perm);
            permissions.remove(perm);
        }
    }

    @Override
    public void removePermission(Module module, Permission permission)
    {
        this.removePermission(module, permission.getName());
    }

    @Override
    public void removePermissions(Module module)
    {
        expectNotNull(module, "The module must not be null!");

        Set<String> removedPerms = this.modulePermissionMap.remove(module);
        if (removedPerms != null)
        {
            permissions.keySet().removeAll(removedPerms);
        }
    }

    @Override
    public void clean()
    {
        this.permissions.clear();
        this.modulePermissionMap.clear();
    }
}
