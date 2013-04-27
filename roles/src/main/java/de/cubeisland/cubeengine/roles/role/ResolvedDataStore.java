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
package de.cubeisland.cubeengine.roles.role;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import de.cubeisland.cubeengine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedPermission;

import gnu.trove.map.hash.THashMap;

public class ResolvedDataStore
{
    protected Map<String,ResolvedPermission> permissions;
    protected Map<String,ResolvedMetadata> metadata;
    protected Set<Role> assignedRoles;

    protected Set<ResolvedDataStore> dependentData;
    protected RawDataStore rawDataStore;
    private boolean dirty = true;

    public ResolvedDataStore(RawDataStore store)
    {
        this.rawDataStore = store;
    }

    private void inheritFrom(Set<Role> assignedRoles)
    {
        this.assignedRoles = assignedRoles;
        this.dependentData = new HashSet<ResolvedDataStore>();
        for (Role role : assignedRoles)
        {
            if (role.isDirty())
            {
                System.out.print("Role to assign is dirty! " + role.getName());
            }
            role.resolvedData.dependentData.add(this);
        }
    }

    public boolean isDirty()
    {
        return dirty;
    }

    protected void makeDirty()
    {
        this.dirty = true;
        this.makeChildsDirty();
    }

    private void doCalculate(Map<String, Boolean> perms, Map<String, String> metadata, Set<Role> assignedRoles)
    {
        this.inheritFrom(assignedRoles);
        // First calculate/apply direct Perm & Metadata
        this.permissions = new THashMap<String, ResolvedPermission>();
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                Map<String, Boolean> subperms = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
                for (Entry<String, Boolean> subEntry : subperms.entrySet())
                {
                    this.permissions.put(subEntry.getKey(), new ResolvedPermission(rawDataStore,subEntry.getKey(),subEntry.getValue()));
                }
            }
            this.permissions.put(entry.getKey(), new ResolvedPermission(rawDataStore,entry.getKey(),entry.getValue()));
        }
        this.metadata = new THashMap<String, ResolvedMetadata>();
        for (Entry<String, String> entry : metadata.entrySet())
        {
            this.metadata.put(entry.getKey(), new ResolvedMetadata(this.rawDataStore, entry.getKey(), entry.getValue()));
        }
        // Then merge inheritance Perm & Metadata
        if (assignedRoles != null && !assignedRoles.isEmpty())
        {
            Map<String, ResolvedPermission> mergePerm = new HashMap<String, ResolvedPermission>();
            Map<String, ResolvedMetadata> mergeMeta = new HashMap<String, ResolvedMetadata>();
            for (Role toMerge : assignedRoles)
            {
                for (Entry<String, ResolvedPermission> entry : toMerge.resolvedData.permissions.entrySet())
                {
                    if (this.permissions.containsKey(entry.getKey())) // overwritten by role
                    {
                        continue;
                    }
                    if (mergePerm.containsKey(entry.getKey())) // handle conflict
                    {
                        if (entry.getValue().getPriorityValue() < mergePerm.get(entry.getKey()).getPriorityValue())
                        {
                            continue; // lower priority -> ignore
                        }
                    }
                    mergePerm.put(entry.getKey(),entry.getValue());
                }
                for (Entry<String, ResolvedMetadata> entry : toMerge.resolvedData.metadata.entrySet())
                {
                    if (this.metadata.containsKey(entry.getKey())) // overwritten by role
                    {
                        continue;
                    }
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
            // And finally apply the data.
            this.permissions.putAll(mergePerm);
            this.metadata.putAll(mergeMeta);
        }
        this.dirty = false;
    }

    protected void calculate(RawDataStore temporary, Set<Role> assignedRoles)
    {
        this.calculate(assignedRoles);
        for (Entry<String, Boolean> entry : temporary.getRawPermissions().entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                Map<String, Boolean> subperms = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
                for (Entry<String, Boolean> subEntry : subperms.entrySet())
                {
                    this.permissions.put(subEntry.getKey(), new ResolvedPermission(temporary,subEntry.getKey(),subEntry.getValue()));
                }
            }
            this.permissions.put(entry.getKey(), new ResolvedPermission(temporary,entry.getKey(),entry.getValue()));
        }
        for (Entry<String, String> entry : temporary.getRawMetadata().entrySet())
        {
            this.metadata.put(entry.getKey(), new ResolvedMetadata(temporary, entry.getKey(), entry.getValue()));
        }
    }

    protected void calculate(Set<Role> assignedRoles)
    {
        this.doCalculate(this.rawDataStore.getRawPermissions(),this.rawDataStore.getRawMetadata(),assignedRoles);
    }

    private void resolveBukkitPermission(String name, boolean set, Map<String, Boolean> resolvedPermissions)
    {
        Permission bukkitPerm = Bukkit.getPluginManager().getPermission(name);
        if (bukkitPerm == null)
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

    private void makeChildsDirty()
    {
        for (ResolvedDataStore resolvedData : this.dependentData)
        {
            resolvedData.makeDirty();
        }
    }

    public boolean inheritsFrom(Role other)
    {
        if (this.assignedRoles.contains(other))
        {
            return true;
        }
        for (Role role : assignedRoles)
        {
            if (role.inheritsFrom(other))
            {
                return true;
            }
        }
        return false;
    }

    protected void performDeleteRole()
    {
        if (this.rawDataStore instanceof Role)
        {
            // remove instances in parents
            for (Role parentRole : this.assignedRoles)
            {
                parentRole.resolvedData.dependentData.remove(this.rawDataStore);
            }
            // remove from config in children
            for (ResolvedDataStore subData : this.dependentData)
            {
                subData.rawDataStore.removeRole((Role)this.rawDataStore);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Deleting is only supported by Config-Roles");
        }
    }

    public Map<String, Boolean> getResolvedPermissions()
    {
        Map <String, Boolean> result = new HashMap<String, Boolean>();
        for (ResolvedPermission perm : this.permissions.values())
        {
            result.put(perm.getKey(), perm.isSet());
        }
        return result;
    }

    public Map<String, String> getResolvedMetadata()
    {
        Map <String, String> result = new HashMap<String, String>();
        for (ResolvedMetadata metadata : this.metadata.values())
        {
            result.put(metadata.getKey(), metadata.getValue());
        }
        return result;
    }
}
