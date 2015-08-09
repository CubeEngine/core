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
package de.cubeisland.engine.service.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.service.filesystem.FileManager;
import de.cubeisland.engine.service.logging.LoggingUtil;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.ServiceReference;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Texts;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

@ServiceImpl(value = PermissionManager.class)
@Version(1)
public class SpongePermissionManager implements PermissionManager
{
    // TODO Modularity Events e.g. to react on shutdown of modules

    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Map<Module, PermissionDescription> modulePermissions = new HashMap<>();
    private final Map<String, PermissionDescription> permissions = new HashMap<>();

    private final Log logger;
    private final Object plugin;
    private Game game;

    private boolean registered = false;

    @Inject private PermissionService permissionService;

    @Inject
    public SpongePermissionManager(Game game, LogFactory factory, ThreadFactory threadFactory, FileManager fm)
    {
        this.game = game;
        this.logger = factory.getLog(CoreModule.class, "Permissions");
        this.logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(fm, "Permissions"), LoggingUtil.getFileFormat(
            false, false), false, LoggingUtil.getCycler(), threadFactory));
        plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance();
    }

    private void registerBasePermission()
    {
        ServiceReference<PermissionService> service = game.getServiceManager().potentiallyProvide(PermissionService.class);
        service.executeWhenPresent(input -> {
            Builder builder = input.newDescriptionBuilder(plugin).orNull();
            if (builder == null)
            {
                return false;
            }
            builder.id("cubeengine");
            builder.description(Texts.of("Base Permission for the CubeEngine Plugin")); // TODO TRANSLATABLE
            builder.assign("permission:*", true);
            builder.register();
            return true;
        });

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
    public PermissionDescription register(Module module, String permission, String description, PermissionDescription parent, PermissionDescription... assigned)
    {
        if (!registered)
        {
            registered = true;
            registerBasePermission();
        }
        Builder builder = game.getServiceManager().provideUnchecked(PermissionService.class).newDescriptionBuilder(plugin).orNull();
        Set<String> perms = modulePermissionMap.get(module);
        if (perms == null)
        {
            perms = new HashSet<>();
            modulePermissionMap.put(module, perms);
        }
        perms.add(permission);

        if (builder == null)
        {
            return null;
        }
        if (parent == null)
        {
            parent = getModulePermission(module);
        }
        permission = parent.getId() + "." + permission;
        builder.id(permission);
        if (description != null)
        {
            builder.description(Texts.of(description));
        }
        for (PermissionDescription assignment : assigned)
        {
            builder.assign("permission:" + assignment.getId(), true);
        }
        PermissionDescription registered = builder.register();
        permissions.put(permission, registered);
        return registered;
    }

    @Override
    public PermissionDescription getModulePermission(Module module)
    {
        PermissionDescription perm = modulePermissions.get(module);
        if (perm == null)
        {
            Builder builder = permissionService.newDescriptionBuilder(plugin).orNull();
            if (builder == null)
            {
                return null;
            }
            String moduleName = module.getInformation().getName();
            builder.id("cubeengine." + moduleName.toLowerCase());
            builder.description(Texts.of(String.format("Base Permission for the %s Module", moduleName))); // TODO TRANSLATABLE
            perm = builder.register();
            modulePermissions.put(module, perm);
        }
        return perm;
    }
/*
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
    */

    @Override
    public void cleanup(Module module)
    {
        expectNotNull(module, "The module must not be null!");
        this.modulePermissionMap.remove(module);
    }

    @Override
    public PermissionDescription getPermission(String permission)
    {
        return permissions.get(permission);
    }

    @Override
    public void clean() // TODO shutdown service
    {
        this.permissions.clear();
        this.modulePermissionMap.clear();
    }
}
