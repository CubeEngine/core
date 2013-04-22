package de.cubeisland.cubeengine.roles.role.newRole;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import de.cubeisland.cubeengine.roles.role.newRole.resolved.ResolvedMetadata;
import de.cubeisland.cubeengine.roles.role.newRole.resolved.ResolvedPermission;

import gnu.trove.map.hash.THashMap;

public class ResolvedDataStore
{
    private Map<String,ResolvedPermission> permissions;
    private Map<String,ResolvedMetadata> metadata;
    private Set<Role> parentRoles;
    public Set<Role> childRoles;
    private Role role;
    private boolean dirty = true;

    public ResolvedDataStore(Role role)
    {
        this.role = role;
    }

    private void inheritFrom(TreeSet<Role> parentRoles)
    {
        this.parentRoles = parentRoles;
        this.childRoles = new TreeSet<Role>();
        for (Role role : parentRoles)
        {
            role.resolvedData.childRoles.add(role);
        }
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void makeDirty()
    {
        this.dirty = true;
        this.makeChildsDirty();
    }

    public void calculateWithParentRoles(TreeSet<Role> parentRoles)
    {
        this.inheritFrom(parentRoles);
        // First calculate/apply direct Perm & Metadata
        this.permissions = new THashMap<String, ResolvedPermission>();
        for (Entry<String, Boolean> entry : role.getRawPermissions().entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                Map<String, Boolean> subperms = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
                for (Entry<String, Boolean> subEntry : subperms.entrySet())
                {
                    this.permissions.put(subEntry.getKey(), new ResolvedPermission(role,subEntry.getKey(),subEntry.getValue()));
                }
            }
            this.permissions.put(entry.getKey(), new ResolvedPermission(role,entry.getKey(),entry.getValue()));
        }
        this.metadata = new THashMap<String, ResolvedMetadata>();
        for (Entry<String, String> entry : role.getRawMetadata().entrySet())
        {
            this.metadata.put(entry.getKey(), new ResolvedMetadata(this.role, entry.getKey(), entry.getValue()));
        }
        // Then merge inheritance Perm & Metadata
        if (parentRoles != null && !parentRoles.isEmpty())
        {
            Map<String, ResolvedPermission> mergePerm = new HashMap<String, ResolvedPermission>();
            Map<String, ResolvedMetadata> mergeMeta = new HashMap<String, ResolvedMetadata>();
            for (Role toMerge : parentRoles)
            {
                for (Entry<String, ResolvedPermission> entry : toMerge.resolvedData.permissions.entrySet())
                {
                    if (this.permissions.containsKey(entry.getKey())) // overwritten by role
                    {
                        continue;
                    }
                    if (mergePerm.containsKey(entry.getKey())) // handle conflict
                    {
                        if (entry.getValue().getPriorityValue() < this.permissions.get(entry.getKey()).getPriorityValue())
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

    public void performRename(String newName)
    {
        for (Role role : this.childRoles)
        {
            role.removeParentRole(this.role);
        }
        this.role.config.roleName = newName;
        for (Role role : this.childRoles)
        {
            role.addParentRole(this.role);
        }
    }

    private void makeChildsDirty()
    {
        for (Role role : this.childRoles)
        {
            role.resolvedData.makeDirty();
            role.resolvedData.makeChildsDirty();
        }
    }

    public boolean inheritsFrom(Role other)
    {
        if (this.parentRoles.contains(other))
        {
            return true;
        }
        for (Role role : parentRoles)
        {
            if (role.inheritsFrom(other))
            {
                return true;
            }
        }
        return false;

    }

    public void performDeleteRole()
    {
        // remove instances in parents
        for (Role parentRole : this.parentRoles)
        {
            parentRole.resolvedData.childRoles.remove(this.role);
        }
        // remove from config in children
        for (Role childRole : this.childRoles)
        {
            childRole.removeParentRole(this.role);
        }
    }
}
