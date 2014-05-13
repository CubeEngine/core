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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.bukkit.World;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.roles.config.Priority;
import de.cubeisland.engine.roles.config.RoleConfig;
import de.cubeisland.engine.roles.exception.CircularRoleDependencyException;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;

public class Role extends ResolvedDataHolder implements Comparable<Role>
{
    protected final RoleConfig config;
    protected final Permission rolePermission;

    private boolean isDefaultRole = false;

    public Role(RolesManager manager, RoleProvider provider, RoleConfig config)
    {
        super(manager, provider);
        this.config = config;
        this.rolePermission = provider.basePerm.child(config.roleName);
        this.module.getCore().getPermissionManager().registerPermission(this.module, this.rolePermission);
    }

    @Override
    public void calculate(Stack<String> roleStack)
    {
        if (this.isDirty())
        {
            super.calculate(roleStack);
            this.module.getLog().debug("   - {} calculated!", this.getName());
            for (ResolvedDataHolder role : this.dependentRoles)
            {
                role.makeDirty();
            }
            for (ResolvedDataHolder role : this.dependentRoles)
            {
                role.calculate(new Stack<String>());
            }
        }
    }

    public boolean rename(String newName)
    {
        if (this.provider.getRole(newName) != null)
        {
            return false;
        }
        this.makeDirty();
        if (this.isGlobal())
        {
            this.manager.dsl.update(TABLE_ROLE).set(DSL.row(TABLE_ROLE.ROLENAME), DSL.row("g:" + newName)).
                where(TABLE_ROLE.ROLENAME.eq(this.getName())).execute();
        }
        else
        {
            Set<UInteger> worldMirrors = new HashSet<>();
            for (Entry<World, Triplet<Boolean, Boolean, Boolean>> entry : ((WorldRoleProvider)provider).getWorldMirrors().entrySet())
            {
                if (entry.getValue().getSecond())
                {
                    worldMirrors.add(UInteger
                                         .valueOf(this.module.getCore().getWorldManager().getWorldId(entry.getKey())));
                }
            }
            this.manager.dsl.update(TABLE_ROLE).set(TABLE_ROLE.ROLENAME, newName).
                where(TABLE_ROLE.ROLENAME.eq(this.getName()), TABLE_ROLE.WORLDID.in(worldMirrors)).execute();
        }
        this.delete();
        this.config.roleName = newName;
        this.provider.addRole(this);
        for (Role role : this.resolvedRoles)
        {
            role.dependentRoles.add(this);
        }
        for (ResolvedDataHolder dataHolder : this.dependentRoles)
        {
            dataHolder.assignRole(this);
        }
        this.config.setTarget(new File(this.config.getTarget().getParent(), this.config.roleName + ".yml"));
        this.save();
        return true;
    }

    public void delete()
    {
        for (Role role : this.resolvedRoles)
        {
            role.dependentRoles.remove(this);
        }
        for (ResolvedDataHolder dataHolder : this.dependentRoles)
        {
            dataHolder.removeRole(this);
        }
        if (this.isGlobal())
        {
            this.manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.ROLENAME.eq(this.getName())).execute();
        }
        else
        {
            Set<UInteger> worldMirrors = new HashSet<>();
            for (Entry<World, Triplet<Boolean, Boolean, Boolean>> entry : ((WorldRoleProvider)provider).getWorldMirrors().entrySet())
            {
                if (entry.getValue().getSecond())
                {
                    worldMirrors.add(UInteger.valueOf(this.module.getCore().getWorldManager().getWorldId(entry.getKey())));
                }
            }
            this.manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.ROLENAME.eq(this.getName()),
                                                      TABLE_ROLE.WORLDID.in(worldMirrors)).execute();
        }
        this.provider.removeRole(this);
        try
        {
            Files.delete(this.config.getTarget().toPath());
        }
        catch (IOException e)
        {
            this.module.getLog().error(e, "Could not delete role {}!", this.config.getTarget().getName());
        }
    }

    @Override
    public String getName()
    {
        if (this.isGlobal())
        {
            return "g:" + this.config.roleName;
        }
        else
        {
            return this.config.roleName;
        }
    }

    public boolean isGlobal()
    {
        return this.provider instanceof GlobalRoleProvider;
    }

    public int getPriorityValue()
    {
        return this.config.priority.value;
    }

    public Role setPriorityValue(int value)
    {
        this.makeDirty();
        this.config.priority = Priority.getByValue(value);
        return this;
    }

    public void save()
    {
        this.config.save();
        for (ResolvedDataHolder dataHolder : this.dependentRoles)
        {
            if (dataHolder instanceof Role)
            {
                ((Role)dataHolder).save();
            }
        }
        this.calculate(new Stack<String>());
    }

    @Override
    public Map<String, Boolean> getRawPermissions()
    {
        return Collections.unmodifiableMap(this.config.perms.getPermissions());
    }

    @Override
    public Map<String, String> getRawMetadata()
    {
        return Collections.unmodifiableMap(this.config.metadata);
    }

    @Override
    public Set<String> getRawRoles()
    {
        return Collections.unmodifiableSet(this.config.parents);
    }

    @Override
    public PermissionValue setPermission(String perm, PermissionValue set)
    {
        this.makeDirty();
        return this.config.perms.setPermission(perm, set);
    }

    @Override
    public String setMetadata(String key, String value)
    {
        this.makeDirty();
        return this.config.metadata.put(key, value);
    }

    @Override
    public boolean removeMetadata(String key)
    {
        this.makeDirty();
        return this.config.metadata.remove(key) != null;
    }

    @Override
    public boolean assignRole(Role role)
    {
        if (this.inheritsFrom(role))
        {
            throw new CircularRoleDependencyException("Cannot add parentrole!");
        }
        this.makeDirty();
        return this.config.parents.add(role.getName());
    }

    @Override
    public boolean removeRole(Role role)
    {
        this.makeDirty();
        return this.config.parents.remove(role.getName());
    }

    @Override
    public void clearPermissions()
    {
        this.makeDirty();
        this.config.perms.getPermissions().clear();
    }

    @Override
    public void clearMetadata()
    {
        this.makeDirty();
        this.config.metadata.clear();
    }

    @Override
    public void clearRoles()
    {
        this.makeDirty();
        this.config.parents.clear();
    }

    public void reload()
    {
        this.config.reload();
        this.makeDirty();
    }

    public boolean isDefaultRole()
    {
        return this.isDefaultRole;
    }

    public boolean setDefaultRole(boolean set)
    {
        if (!this.isGlobal())
        {
            if (this.isDefaultRole != set)
            {
                this.isDefaultRole = set;
                ((WorldRoleProvider)this.provider).setDefaultRole(this, set);
                return true;
            }
        }
        return false;
    }

    public boolean canAssignAndRemove(Permissible permissible)
    {
        return this.rolePermission.isAuthorized(permissible);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Role role = (Role)o;

        if (getName() != null ? !getName().equals(role.getName()) : role.getName() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public int compareTo(Role o)
    {
        return this.getPriorityValue() - o.getPriorityValue();
    }
}
