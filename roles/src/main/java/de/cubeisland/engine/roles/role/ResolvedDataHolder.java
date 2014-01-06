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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.exception.CircularRoleDependencyException;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;
import gnu.trove.map.hash.THashMap;

public abstract class ResolvedDataHolder extends TempDataStore
{
    protected final Roles module;
    protected final RolesManager manager;
    protected final RoleProvider provider;

    protected Map<String, ResolvedPermission> resolvedPermissions;
    protected Map<String, ResolvedMetadata> resolvedMetadata;
    protected TreeSet<Role> resolvedRoles;

    protected Set<ResolvedDataHolder> dependentRoles;

    private boolean dirty = true;

    protected ResolvedDataHolder(RolesManager manager, RoleProvider provider)
    {
        this.manager = manager;
        this.module = manager.module;
        this.provider = provider;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void makeDirty()
    {
        this.dirty = true;
    }

    @Override
    public void calculate(Stack<String> roleStack)
    {
        if (!this.isDirty())
        {
            return;
        }
        if (roleStack.contains(this.getName()))
        {
            throw new CircularRoleDependencyException("Cannot load role! Circular Dependency detected in " +
                  this.getName() + "\n" + StringUtils.implode(", ", roleStack));
        }
        roleStack.push(this.getName());
        this.resolveRoles(roleStack);
        this.resolvedPermissions = new HashMap<>();
        this.calculatePerms(this.getRawPermissions());
        this.calculatePerms(this.getRawTempPermissions());
        this.resolvedMetadata = new HashMap<>();
        this.calculateMetadata(this.getRawMetadata());
        this.calculateMetadata(this.getRawTempMetaData());
        this.calculateRoles();

        this.dirty = false;
        roleStack.pop();
    }

    private void calculatePerms(Map<String, Boolean> perms)
    {
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            Map<String, Boolean> subperms = new HashMap<>();
            this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
            for (Entry<String, Boolean> subEntry : subperms.entrySet())
            {
                this.resolvedPermissions.put(subEntry.getKey(), new ResolvedPermission(this,subEntry.getKey(),subEntry.getValue(), entry.getKey()));
            }
            this.resolvedPermissions.put(entry.getKey(), new ResolvedPermission(this,entry.getKey(),entry.getValue(), null));
        }
    }

    private void calculateMetadata(Map<String, String> metadata)
    {
        for (Entry<String, String> entry : metadata.entrySet())
        {
            this.resolvedMetadata.put(entry.getKey(), new ResolvedMetadata(this, entry.getKey(), entry.getValue()));
        }
    }

    private void calculateRoles()
    {
        Map<String, ResolvedPermission> mergePerm = new HashMap<>();
        Map<String, ResolvedMetadata> mergeMeta = new HashMap<>();
        for (Role toMerge : this.resolvedRoles)
        {
            for (Entry<String, ResolvedPermission> entry : toMerge.getPermissions().entrySet())
            {
                if (!this.resolvedPermissions.containsKey(entry.getKey())) // overwritten by role
                {
                    if (mergePerm.containsKey(entry.getKey())) // handle conflict
                    {
                        if (entry.getValue().getPriorityValue() < mergePerm.get(entry.getKey()).getPriorityValue())
                        {
                            continue; // lower priority -> ignore
                        }
                    }
                    mergePerm.put(entry.getKey(), entry.getValue());
                }
            }
            for (Entry<String, ResolvedMetadata> entry : toMerge.getMetadata().entrySet())
            {
                if (!this.resolvedMetadata.containsKey(entry.getKey())) // overwritten by role
                {
                    if (mergeMeta.containsKey(entry.getKey()))
                    {
                        if (entry.getValue().getPriorityValue() < mergeMeta.get(entry.getKey()).getPriorityValue())
                        {
                            continue;
                        }
                    }
                    mergeMeta.put(entry.getKey(), entry.getValue());
                }
            }
        }
        // And finally apply the data.
        this.resolvedPermissions.putAll(mergePerm);
        this.resolvedMetadata.putAll(mergeMeta);
    }

    private void resolveBukkitPermission(String name, boolean set, Map<String, Boolean> resolvedPermissions)
    {
        Permission bukkitPerm = Bukkit.getPluginManager().getPermission(name);
        if (bukkitPerm == null) // not registered permission (probably a cubeengine * perm)
        {
            if (name.endsWith(".*"))
            {
                // manually search for child-perms...
                String baseName = name.substring(0, name.indexOf(".*"));
                for (Permission permission : Bukkit.getPluginManager().getPermissions())
                {
                    if (permission.getName().startsWith(baseName))
                    {
                        resolvedPermissions.put(permission.getName(), set);
                    }
                }
                resolvedPermissions.put(name, set);
            }
            return;
        }
        Map<String, Boolean> childPerm = bukkitPerm.getChildren();
        for (String permKey : childPerm.keySet())
        {
            this.resolveBukkitPermission(permKey, set, resolvedPermissions);
            resolvedPermissions.put(permKey, set && childPerm.get(permKey));
        }
    }

    private void resolveRoles(Stack<String> roleStack)
    {
        List<Role> roles = new ArrayList<>();
        for (String roleName : this.getRawRoles())
        {
            Role role = provider.getRole(roleName);
            if (role == null)
            {
                this.roleMissing(roleName, false);
                continue;
            }
            roles.add(role);
        }
        for (String roleName : this.getRawTempRoles())
        {
            Role role = provider.getRole(roleName);
            if (role == null)
            {
                this.roleMissing(roleName, false);
                continue;
            }
            roles.add(role);
        }
        TreeSet<Role> result = new TreeSet<>();
        for (Role role : roles)
        {
            if (role.isDirty())
            {
                try
                {
                    role.calculate(roleStack);
                }
                catch (CircularRoleDependencyException e)
                {
                    this.module.getLog().warn(e, "A CircularRoleDependencyException occurred");
                }
            }
            result.add(role);
        }
        this.inheritRoles(result);
    }

    private void inheritRoles(TreeSet<Role> roles)
    {
        this.resolvedRoles = roles;
        if (dependentRoles == null)
        {
            this.dependentRoles = new HashSet<>();
        }
        for (Role role : roles)
        {
            if (role.isDirty())
            {
                this.module.getLog().debug("Role to assign is dirty! {}", role.getName());
            }
            role.dependentRoles.add(this);
        }
    }

    protected void roleMissing(String roleName, boolean temp) // TODO override send to user
    {
        if (temp)
        {
            this.module.getLog().warn("The role {} is not available in {}", roleName, provider.getMainWorld().getName());
        }
        else
        {
            this.module.getLog().warn("The role {} is not available in {}", roleName, provider.getMainWorld().getName());
        }
        //this.getHolder().sendTranslated("&cYour role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
        //this.getHolder().sendTranslated("&4You should report this to an administrator!");

        //this.getHolder().sendTranslated("&cYour temporary role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
        //this.getHolder().sendTranslated("&4You should report this to an administrator!");
    }

    @Override
    public Map<String, ResolvedPermission> getPermissions()
    {
        return Collections.unmodifiableMap(this.resolvedPermissions);
    }

    @Override
    public Map<String, ResolvedMetadata> getMetadata()
    {
        return Collections.unmodifiableMap(this.resolvedMetadata);
    }

    @Override
    public Set<Role> getRoles()
    {
        return Collections.unmodifiableSet(this.resolvedRoles);
    }

    @Override
    public boolean inheritsFrom(Role other)
    {
        if (this.resolvedRoles.contains(other))
        {
            return true;
        }
        for (Role role : this.resolvedRoles)
        {
            if (role.inheritsFrom(other))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Boolean> getAllRawPermissions()
    {
        Map<String,Boolean> result = new THashMap<>();
        for (Role assignedRole : this.resolvedRoles)
        {
            result.putAll(assignedRole.getAllRawPermissions());
        }
        result.putAll(this.getRawPermissions());
        return result;
    }

    @Override
    public Map<String, String> getAllRawMetadata()
    {
        Map<String,String> result = new THashMap<>();
        for (Role assignedRole : this.resolvedRoles)
        {
            result.putAll(assignedRole.getAllRawMetadata());
        }
        result.putAll(this.getRawMetadata());
        return result;
    }

    protected Map<String, Boolean> getResolvedPermissions()
    {
        Map<String, Boolean> result = new HashMap<>();
        for (Entry<String, ResolvedPermission> entry : this.getPermissions().entrySet())
        {
            result.put(entry.getKey(), entry.getValue().isSet());
        }
        return result;
    }
}
