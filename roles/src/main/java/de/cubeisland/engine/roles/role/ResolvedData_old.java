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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

import gnu.trove.map.hash.THashMap;

public class ResolvedData_old
{
    protected Map<String,ResolvedPermission> permissions;
    protected Map<String,ResolvedMetadata> metadata;
    protected TreeSet<Role_old> assignedRoles;

    protected Set<ResolvedData_old> dependentData;
    protected DataStore rawDataStore;
    private boolean dirty = true;

    public ResolvedData_old(DataStore store)
    {
        this.rawDataStore = store;
    }

    private void inheritFrom(Set<Role_old> assignedRoles)
    {
        this.assignedRoles = new TreeSet<>(assignedRoles);
        this.dependentData = new HashSet<>();
        for (Role_old role : assignedRoles)
        {
             if (role.isDirty())
            {
                CubeEngine.getLog().debug("Role to assign is dirty! {}", role.getName());
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

    private void doCalculate(Map<String, Boolean> perms, Map<String, String> metadata, Set<Role_old> assignedRoles)
    {
        this.inheritFrom(assignedRoles);
        // First calculate/apply direct Perm & Metadata
        this.permissions = new THashMap<>();
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            Map<String, Boolean> subperms = new HashMap<>();
            this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
            for (Entry<String, Boolean> subEntry : subperms.entrySet())
            {
                this.permissions.put(subEntry.getKey(), new ResolvedPermission(rawDataStore,subEntry.getKey(),subEntry.getValue(), entry.getKey()));
            }
            this.permissions.put(entry.getKey(), new ResolvedPermission(rawDataStore,entry.getKey(),entry.getValue(), null));
        }
        this.metadata = new THashMap<>();
        for (Entry<String, String> entry : metadata.entrySet())
        {
            this.metadata.put(entry.getKey(), new ResolvedMetadata(this.rawDataStore, entry.getKey(), entry.getValue()));
        }
        // Then merge inheritance Perm & Metadata
        if (assignedRoles != null && !assignedRoles.isEmpty())
        {
            Map<String, ResolvedPermission> mergePerm = new HashMap<>();
            Map<String, ResolvedMetadata> mergeMeta = new HashMap<>();
            for (Role_old toMerge : assignedRoles)
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

    protected void calculate(DataStore temporary, Set<Role_old> assignedRoles)
    {
        this.calculate(assignedRoles);
        for (Entry<String, Boolean> entry : temporary.getRawPermissions().entrySet())
        {
            Map<String, Boolean> subperms = new HashMap<>();
            this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
            for (Entry<String, Boolean> subEntry : subperms.entrySet())
            {
                this.permissions.put(subEntry.getKey(), new ResolvedPermission(temporary,subEntry.getKey(),subEntry.getValue(), entry.getKey()));
            }
            this.permissions.put(entry.getKey(), new ResolvedPermission(temporary,entry.getKey(),entry.getValue(), null));
        }
        for (Entry<String, String> entry : temporary.getRawMetadata().entrySet())
        {
            this.metadata.put(entry.getKey(), new ResolvedMetadata(temporary, entry.getKey(), entry.getValue()));
        }
    }

    protected void calculate(Set<Role_old> assignedRoles)
    {
        this.doCalculate(this.rawDataStore.getRawPermissions(),this.rawDataStore.getRawMetadata(),assignedRoles);
    }


    private void makeChildsDirty()
    {
        for (ResolvedData_old resolvedData : this.dependentData)
        {
            resolvedData.makeDirty();
        }
    }

    public boolean inheritsFrom(Role_old other)
    {
        if (this.assignedRoles.contains(other))
        {
            return true;
        }
        for (Role_old role : assignedRoles)
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
        if (this.rawDataStore instanceof Role_old)
        {
            // remove instances in parents
            for (Role_old parentRole : this.assignedRoles)
            {
                parentRole.resolvedData.dependentData.remove(((Role_old)this.rawDataStore).resolvedData);
            }
            // remove from config in children
            for (ResolvedData_old subData : this.dependentData)
            {
                subData.rawDataStore.removeRole((Role_old)this.rawDataStore);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Deleting is only supported by Config-Roles");
        }
    }

    public Map<String, Boolean> getResolvedPermissions()
    {
        Map <String, Boolean> result = new HashMap<>();
        for (ResolvedPermission perm : this.permissions.values())
        {
            result.put(perm.getKey(), perm.isSet());
        }
        return result;
    }

    public Map<String, String> getResolvedMetadata()
    {
        Map <String, String> result = new HashMap<>();
        for (ResolvedMetadata metadata : this.metadata.values())
        {
            result.put(metadata.getKey(), metadata.getValue());
        }
        return result;
    }
}
