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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.ModuleHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

/**
 * Registers permissions to the server.
 */
@ServiceProvider(PermissionManager.class)
@Version(1)
public class PermissionManager implements ModuleHandler
{
    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Map<Module, PermissionDescription> modulePermissions = new HashMap<>();
    private final Map<String, PermissionDescription> permissions = new HashMap<>();

    private final Object plugin;

    private boolean registered = false;

    @Inject private PermissionService permissionService;

    @Inject
    public PermissionManager(Game game, Modularity modularity)
    {
        plugin = game.getPluginManager().getPlugin("org.cubeengine").get().getInstance().get();
        modularity.registerHandler(this);
    }

     private void registerBasePermission()
    {
        if (registered)
        {
            return;
        }

        registered = true;

        PermissionDescription.Builder builder = this.permissionService.newDescriptionBuilder(plugin).orElse(null);
        if (builder == null)
        {
            return;
        }
        builder.id("cubeengine");
        builder.description(Text.of("Base Permission for the CubeEngine Plugin")); // TODO TRANSLATABLE
        builder.assign("permission:*", true);
        builder.register();
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

    /**
     * Registers a permission
     *
     * @param permission the permission
     */
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
                builder.description(Text.of(description));
            }
            return builder;
        }).map(b -> assignAndRegister(b, toAssign)).orElse(null);
    }


    private PermissionDescription assignAndRegister(PermissionDescription.Builder builder, Stream<String> assign)
    {
        assign.forEach(s -> builder.assign(s, true));
        PermissionDescription register = builder.register();
        permissions.put(register.getId(), register);
        return register;
    }

    public PermissionDescription registerS(Module module, String permission, String description,
                                           PermissionDescription parent, String... assigned)
    {
        String parentId = getModulePermission(module).getId() + ".";
        Stream<String> toAssign = Arrays.asList(assigned).stream().map(s -> "permission:" + parentId + s);
        return register(module, getPermission(module, permission, parent), description, toAssign);
    }

    public PermissionDescription getModulePermission(Module module)
    {
        PermissionDescription perm = modulePermissions.get(module);
        if (perm == null)
        {
            PermissionDescription.Builder builder = permissionService.newDescriptionBuilder(plugin).orElse(null);
            if (builder == null)
            {
                return null;
            }
            String moduleName = module.getInformation().getName();
            builder.id("cubeengine." + moduleName.toLowerCase());
            builder.description(Text.of(String.format("Base Permission for the %s Module", moduleName))); // TODO TRANSLATABLE
            perm = builder.register();
            modulePermissions.put(module, perm);
        }
        return perm;
    }

    /**
     * Returns the permission node with given name or {@link Optional#empty()} ()} if not found
     * @param permission the permissions name
     * @return the permission if found
     */
    public PermissionDescription getPermission(String permission)
    {
        return permissions.get(permission);
    }


    @Override
    public void onEnable(Module module)
    {
        for (Field field : module.getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(ModulePermissions.class))
            {
                try
                {
                    Object container = field.getType().getConstructors()[0].newInstance(module);
                    field.setAccessible(true);
                    field.set(module, container);
                }
                catch (IllegalAccessException | InstantiationException | InvocationTargetException e)
                {
                    throw new IllegalStateException("Could not set configuration for " + module.getInformation().getName(), e);
                }
            }
        }
    }

    @Override
    public void onDisable(Module module)
    {
        this.modulePermissionMap.remove(module);
        // It is not possible to remove registered PermissionDescriptions from Sponge
    }

    public PermissionDescription getPermission(Module module, String permission)
    {
        PermissionDescription perm = getPermission(getModulePermission(module).getId() + "." + permission);
        if (perm == null)
        {
            throw new IllegalArgumentException("Permission does not exists in this module or is not yet registered");
        }
        return perm;
    }
}
