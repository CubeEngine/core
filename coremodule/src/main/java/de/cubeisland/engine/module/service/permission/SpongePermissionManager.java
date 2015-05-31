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
package de.cubeisland.engine.module.service.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.logging.LoggingUtil;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.sponge.EventManager;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

@ServiceImpl(value = PermissionManager.class)
@Version(1)
public class SpongePermissionManager implements PermissionManager
{
    private final Map<String, Permission> permissions = new HashMap<>();
    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Log logger;
    private EventManager em;

    @Inject
    public SpongePermissionManager(CoreModule core, LogFactory factory, ThreadFactory threadFactory, FileManager fm, EventManager em)
    {
        this.em = em;
        this.logger = factory.getLog(CoreModule.class, "Permissions");
        this.logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(fm, "Permissions"),
                                                  LoggingUtil.getFileFormat(false, false),
                                                  false, LoggingUtil.getCycler(),
                                                  threadFactory));
        this.registerPermission(core, Permission.BASE);
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
    public void clean() // TODO shutdown service
    {
        this.permissions.clear();
        this.modulePermissionMap.clear();
    }
}
