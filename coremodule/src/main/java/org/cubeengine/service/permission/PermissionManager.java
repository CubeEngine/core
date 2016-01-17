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

import com.google.common.base.Preconditions;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registers permissions to the server.
 */
@ServiceProvider(PermissionManager.class)
@Version(1)
public class PermissionManager
{
    // TODO Modularity Events e.g. to react on shutdown of modules

    private final Map<Module, Set<String>> modulePermissionMap = new HashMap<>();
    private final Map<Module, PermissionDescription> modulePermissions = new HashMap<>();
    private final Map<String, PermissionDescription> permissions = new HashMap<>();

    private final Object plugin;
    private Game game;

    private boolean registered = false;

    @Inject private PermissionService permissionService;
    @Inject private I18n i18n;

    @Inject
    public PermissionManager(Game game)
    {
        this.game = game;
        plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance().get();
        game.getEventManager().registerListeners(plugin, this);
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
     * Removes all the permissions of the given module
     *
     * @param module the module
     */
    public void cleanup(Module module)
    {
        checkNotNull(module, "The module must not be null!");
        this.modulePermissionMap.remove(module);
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
}
