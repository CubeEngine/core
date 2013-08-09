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

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.config.RoleConfig;
import de.cubeisland.engine.roles.config.RoleMirror;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;


public class WorldRoleProvider extends RoleProvider
{
    private RoleMirror mirrorConfig;
    private Set<Role> defaultRoles = new HashSet<Role>();

    public WorldRoleProvider(Roles module, RolesManager manager, RoleMirror mirror, long mainWorldId)
    {
        super(module,manager,mainWorldId);
        this.mirrorConfig = mirror;
        this.basePerm = module.getBasePermission().createAbstractChild("world").createAbstractChild(mirror.mainWorld);
    }

    public WorldRoleProvider(Roles module, RolesManager manager, long worldId)
    {
        this(module,manager, new RoleMirror(module, worldId),worldId);
    }

    public void reloadRoles()
    {
        Set<String> defaultRoles = this.module.getConfiguration().defaultRoles.get(mirrorConfig.mainWorld);
        if (defaultRoles == null)
        {
            defaultRoles = new HashSet<String>();
        }
        for (RoleConfig config : this.configs.values())
        {
            Role role = new Role(this, config);
            if (defaultRoles.contains(config.roleName))
            {
                role.isDefaultRole = true;
                this.defaultRoles.add(role);
            }
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
        if (this.defaultRoles.isEmpty())
        {
            this.module.getLog().warn("The role-provider for {} has no default roles!", this.mirrorConfig.mainWorld);
        }
    }


    public TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> getWorldMirrors()
    {
        return this.mirrorConfig.getWorldMirrors();
    }

    public Set<Role> getDefaultRoles()
    {
        return this.defaultRoles;
    }

    public String getMainWorld()
    {
        return this.mirrorConfig.mainWorld;
    }

    @Override
    public void recalculateRoles()
    {
        if (this.roles.isEmpty())
        {
            this.module.getLog().warn("There are no roles for {}", mirrorConfig.mainWorld);
            return;
        }
        this.module.getLog().debug("Calculating Roles of {}...", mirrorConfig.mainWorld);
        super.recalculateRoles();
    }

    @Override
    public File getFolder()
    {
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = new File(this.manager.getRolesFolder(), this.mirrorConfig.mainWorld);
        }
        return this.folder;
    }

    @Override
    public Role getRole(String name)
    {
        name = name.toLowerCase();
        if (name.startsWith("g:"))
        {
            return this.manager.getGlobalProvider().getRole(name.substring(2));
        }
        return super.getRole(name);
    }

    protected void setDefaultRole(Role role, boolean set)
    {
        if (set)
        {
            Set<String> defaultRoles = this.module.getConfiguration().defaultRoles.get(this.getMainWorld());
            defaultRoles.add(role.getName());
            this.defaultRoles.add(role);
            this.module.getConfiguration().save();
        }
        else
        {
            Set<String> defaultRoles = this.module.getConfiguration().defaultRoles.get(this.getMainWorld());
            defaultRoles.remove(role.getName());
            this.defaultRoles.remove(role);
            this.module.getConfiguration().save();
        }
    }

    @Override
    protected void deleteRole(final Role role)
    {
        super.deleteRole(role);
        // Also delete possible mirrors
        this.mirrorConfig.getWorldMirrors().forEachEntry(new TLongObjectProcedure<Triplet<Boolean, Boolean, Boolean>>()
        {
            @Override
            public boolean execute(long worldID, Triplet<Boolean, Boolean, Boolean> mirrors)
            {
                if (mirrors.getFirst() && !mirrors.getSecond()) // roles are mirrored but not assigned roles
                {
                    manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.WORLDID.eq(UInteger.valueOf(worldID)),
                                                              TABLE_ROLE.ROLENAME.eq(role.getName())).execute();
                }
                return true;
            }
        });
    }

    @Override
    protected boolean renameRole(Role role, String newName)
    {
        if (super.renameRole(role,newName))
        {
            Set<UInteger> worldMirrors = new HashSet<>();
            for (long mirrored : this.getWorldMirrors().keys())
            {
                worldMirrors.add(UInteger.valueOf(mirrored));
            }
            this.manager.dsl.update(TABLE_ROLE).set(DSL.row(TABLE_ROLE.ROLENAME), DSL.row(newName)).
                where(TABLE_ROLE.ROLENAME.eq(role.getName()),
                      TABLE_ROLE.WORLDID.in(worldMirrors)).execute();
            return true;
        }
        return false;
    }

    public long[] getMirroredWorlds()
    {
        return this.mirrorConfig.getWorldMirrors().keys();
    }

    protected RoleMirror getMirrorConfig()
    {
        return mirrorConfig;
    }
}
