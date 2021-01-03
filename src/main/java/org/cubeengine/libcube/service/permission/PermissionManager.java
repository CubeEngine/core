/*
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.cubeengine.libcube.ModuleManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registers permissions to the server.
 */
@Singleton
public class PermissionManager
{
    private final PluginContainer plugin;
    private final ModuleManager mm;

    private final Map<Class<?>, Permission> basePermission = new HashMap<>();
    private final Map<String, Permission> permissions = new HashMap<>();
    private final Permission rootPermission;

    private PermissionService permissionService;
    private final Set<Permission> unregistered = new HashSet<>();

    @Inject
    public PermissionManager(Game game, ModuleManager mm, PluginContainer plugin)
    {
        this.plugin = plugin;
        this.mm = mm;
        game.getEventManager().registerListeners(plugin, this);
        rootPermission = register(new Permission("cubeengine", "Root Permission for the CubeEngine Plugin", emptySet())); // TODO translatable
    }

    private String permId(Class owner, String permission, Permission parent)
    {
        return (parent == null ? getBasePermission(owner).getId() : parent.getId()) + "." + permission;
    }

    public Permission register(Class owner, String permission, String description, Permission parent, String... assigned)
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
        Builder builder = permissionService.newDescriptionBuilder(plugin);
        builder.id(permission.getId());
        if (permission.getDesc() != null)
        {
            builder.description(Component.text(permission.getDesc()));
        }
        permission.getExplicitParents().forEach(s -> builder.assign(s, true));
        builder.register();
        return permission;
    }

    public Permission getBasePermission(Class<?> owner)
    {
        Permission perm = basePermission.get(owner);
        if (perm == null)
        {
            String id = mm.getModuleID(owner).orElse(owner.getSimpleName());
            String name = mm.getModuleName(owner).orElse(owner.getSimpleName());
            perm = register(new Permission(permId(null, id.toLowerCase(), rootPermission), "Base Permission for " + name, emptySet())); // TODO translatable
            basePermission.put(owner, perm);
        }
        return perm;
    }

    public Permission getPermission(String permission)
    {
        return permissions.get(permission);
    }

    public Permission getPermission(Class<?> owner, String permission)
    {
        return getPermission(permId(owner, permission, null));
    }

    @Listener
    public void onRegisterService(StartedEngineEvent<Server> event)
    {
        this.register(Sponge.getServer().getServiceProvider().permissionService());
    }

    private void register(PermissionService service)
    {
        this.permissionService = service;
        ArrayList<Permission> unregistered = new ArrayList<>(this.unregistered);
        this.unregistered.clear();
        unregistered.forEach(this::register);
    }

    public Map<String, Permission> getPermissions() {
        return permissions;
    }
}
