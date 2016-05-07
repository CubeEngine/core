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
package org.cubeengine.libcube.service.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

/**
 * Registers permissions to the server.
 */
@ServiceProvider(PermissionManager.class)
@Version(1)
public class PermissionManager
{
    @Inject private Modularity modularity;
    private PluginContainer plugin;

    private final Map<Class, Permission> basePermission = new HashMap<>();
    private final Map<String, Permission> permissions = new HashMap<>();
    private Permission rootPermission;

    private PermissionService permissionService;
    private final Set<Permission> unregistered = new HashSet<>();

    @Inject
    public PermissionManager(PluginContainer plugin)
    {
        this.plugin = plugin;
        Sponge.getEventManager().registerListeners(plugin.getInstance().get(), this);
        rootPermission = register(new Permission("cubeengine", "Root Permission for the CubeEngine Plugin",singleton("*"))); // TODO translatable
    }

    private String permId(Class owner, String permission, Permission parent)
    {
        return (parent == null ? getBasePermission(owner).getId() : parent.getId()) + "." + permission;
    }

    public Permission register(Class owner, String permission, String description, Permission parent, Permission... assigned)
    {
        return register(permId(owner, permission, parent), description, asList(assigned).stream().map(Permission::getId).collect(toSet()));
    }

    public Permission registerS(Class owner, String permission, String description, Permission parent, String... assigned)
    {
        return register(permId(owner, permission, parent), description, new HashSet<>(asList(assigned)));
    }

    private Permission register(String id, String description, Set<String> toAssign)
    {
        return register(new Permission(id, description, toAssign));
    }

    private Permission register(Permission permission)
    {
        permissions.put(permission.getId(), permission);
        if (permissionService == null)
        {
            unregistered.add(permission);
            return permission;
        }
        Optional<Builder> builder = permissionService.newDescriptionBuilder(plugin.getInstance().get());
        if (builder.isPresent())
        {
            Builder build = builder.get().id(permission.getId());
            if (permission.getDesc() != null)
            {
                build.description(Text.of(permission.getDesc()));
            }
            permission.getExplicitParents().stream().map(s -> "permission:" + s).forEach(s -> build.assign(s, true));
            build.register();
        }
        return permission;
    }

    public Permission getBasePermission(Class owner)
    {
        Permission perm = basePermission.get(owner);
        if (perm == null)
        {
            DependencyInformation info = modularity.getLifecycle(owner).getInformation();
            String name = info instanceof ModuleMetadata ? ((ModuleMetadata)info).getName() : info.getClassName();

            perm = register(new Permission(permId(null, name.toLowerCase(), rootPermission), "Base Permission for " + name, emptySet())); // TODO translatable
            basePermission.put(owner, perm);
        }
        return perm;
    }

    public Permission getPermission(String permission)
    {
        return permissions.get(permission);
    }

    public Permission getPermission(Class owner, String permission)
    {
        return getPermission(permId(owner, permission, null));
    }

    @Listener
    public void onRegisterService(ChangeServiceProviderEvent event)
    {
        if (event.getService() == PermissionService.class)
        {
            register((PermissionService)event.getNewProvider());
        }
    }

    private void register(PermissionService service)
    {
        this.permissionService = service;
        ArrayList<Permission> unregistered = new ArrayList<>(this.unregistered);
        this.unregistered.clear();
        unregistered.forEach(this::register);
    }
}
