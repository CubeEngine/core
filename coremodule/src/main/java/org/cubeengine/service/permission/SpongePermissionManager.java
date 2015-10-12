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
package org.cubeengine.service.permission;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.filesystem.FileManager;
import org.cubeengine.service.logging.LoggingUtil;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.ServiceReference;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Texts;

import static org.cubeengine.module.core.contract.Contract.expectNotNull;

@ServiceImpl(value = PermissionManager.class)
@Version(1)
public class SpongePermissionManager implements PermissionManager
{
    // TODO Modularity Events e.g. to react on shutdown of modules

    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Map<Module, PermissionDescription> modulePermissions = new HashMap<>();
    private final Map<String, PermissionDescription> permissions = new HashMap<>();

    private final Object plugin;
    private Game game;

    private boolean registered = false;

    @Inject private PermissionService permissionService;

    @Inject
    public SpongePermissionManager(Game game)
    {
        this.game = game;

        plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance();
    }

    private void registerBasePermission()
    {
        if (!registered)
        {
            registered = true;
            ServiceReference<PermissionService> service = game.getServiceManager().potentiallyProvide(PermissionService.class);
            service.executeWhenPresent(input -> {
                Builder builder = input.newDescriptionBuilder(plugin).orElse(null);
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
        Stream<String> toAssign = Arrays.asList(assigned).stream().map(PermissionDescription::getId).map(s -> "permission:" + s);
        return register(module, getPermission(module, permission, parent), description, toAssign);
    }

    private String getPermission(Module module, String permission, PermissionDescription parent)
    {
        permission = (parent == null ? getModulePermission(module).getId() : parent.getId()) + "." + permission;
        return permission;
    }

    private PermissionDescription register(Module module, String permission, String description, Stream<String> toAssign)
    {
        registerBasePermission();
        Set<String> perms = modulePermissionMap.get(module);
        if (perms == null)
        {
            perms = new HashSet<>();
            modulePermissionMap.put(module, perms);
        }

        perms.add(permission);

        return permissionService.newDescriptionBuilder(plugin).map(builder -> {
            builder.id(permission);
            if (description != null)
            {
                builder.description(Texts.of(description));
            }
            return builder;
        }).map(b -> assignAndRegister(b, toAssign)).orElse(null);
    }


    private PermissionDescription assignAndRegister(Builder builder, Stream<String> assign)
    {
        assign.forEach(s -> builder.assign(s, true));
        PermissionDescription register = builder.register();
        permissions.put(register.getId(), register);
        return register;
    }

    @Override
    public PermissionDescription registerS(Module module, String permission, String description,
                                           PermissionDescription parent, String... assigned)
    {
        String parentId = getModulePermission(module).getId() + ".";
        Stream<String> toAssign = Arrays.asList(assigned).stream().map(s -> "permission:" + parentId + s);
        return register(module, getPermission(module, permission, parent), description, toAssign);
    }

    @Override
    public PermissionDescription getModulePermission(Module module)
    {
        PermissionDescription perm = modulePermissions.get(module);
        if (perm == null)
        {
            Builder builder = permissionService.newDescriptionBuilder(plugin).orElse(null);
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
