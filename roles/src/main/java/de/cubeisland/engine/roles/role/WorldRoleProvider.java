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
package de.cubeisland.engine.roles.role;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.roles.RolesConfig;
import de.cubeisland.engine.roles.config.MirrorConfig;
import de.cubeisland.engine.roles.config.RoleConfig;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

public class WorldRoleProvider extends RoleProvider
{
    private final MirrorConfig mirrorConfig;
    private final Set<Role> defaultRoles = new HashSet<>();
    private Path folder;

    public WorldRoleProvider(RolesManager manager, MirrorConfig mirrorConfig)
    {
        super(manager, manager.module.getBasePermission().childWildcard("world").childWildcard(mirrorConfig.mainWorld
                                                                                                           .getName()));
        this.mirrorConfig = mirrorConfig;
    }

    public WorldRoleProvider(RolesManager manager, World world)
    {
        this(manager, new MirrorConfig(manager.module, world));
    }

    public void reloadRoles()
    {
        Set<String> defaultRoles = this.module.getConfiguration().defaultRoles.get(mirrorConfig.mainWorld.getName());
        if (defaultRoles == null)
        {
            defaultRoles = new HashSet<>();
        }
        for (RoleConfig config : this.configs.values())
        {
            Role role = new Role(manager, this, config);
            if (defaultRoles.contains(config.roleName))
            {
                role.setDefaultRole(true);
                this.defaultRoles.add(role);
            }
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
        if (this.defaultRoles.isEmpty())
        {
            this.module.getLog().warn("The role provider for {} has no default roles!", this.mirrorConfig.mainWorld.getName());
        }
    }

    public Map<World, Triplet<Boolean, Boolean, Boolean>> getWorldMirrors()
    {
        return this.mirrorConfig.getWorldMirrors();
    }

    public Set<Role> getDefaultRoles()
    {
        return Collections.unmodifiableSet(this.defaultRoles);
    }

    public World getMainWorld()
    {
        return this.mirrorConfig.getMainWorld();
    }

    @Override
    public void recalculateRoles()
    {
        if (this.roles.isEmpty())
        {
            this.module.getLog().warn("There are no roles for {}", mirrorConfig.mainWorld.getName());
            return;
        }
        this.module.getLog().debug("Calculating Roles of {}...", mirrorConfig.mainWorld.getName());
        super.recalculateRoles();
    }

    @Override
    protected Path getFolder()
    {
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = this.manager.getRolesFolder().resolve(this.mirrorConfig.mainWorld.getName());
        }
        return this.folder;
    }

    @Override
    public Role getRole(String name)
    {
        expectNotNull(name, "The role name may not be null!");

        name = name.toLowerCase();
        if (name.startsWith("g:"))
        {
            return this.manager.getGlobalProvider().getRole(name.substring(2));
        }
        return super.getRole(name);
    }

    protected void setDefaultRole(Role role, boolean set)
    {
        RolesConfig config = this.module.getConfiguration();
        Set<String> defaultRoles = config.defaultRoles.get(this.getMainWorld().getName());
        if (defaultRoles == null)
        {
            defaultRoles = new HashSet<>();
            config.defaultRoles.put(this.getMainWorld().getName(), defaultRoles);
        }
        if (set)
        {
            defaultRoles.add(role.getName());
            this.defaultRoles.add(role);
        }
        else
        {
            defaultRoles.remove(role.getName());
            this.defaultRoles.remove(role);
        }
        config.save();
    }

    public Set<World> getMirroredWorlds()
    {
        return this.mirrorConfig.getWorldMirrors().keySet();
    }

    protected MirrorConfig getMirrorConfig()
    {
        return mirrorConfig;
    }
}
