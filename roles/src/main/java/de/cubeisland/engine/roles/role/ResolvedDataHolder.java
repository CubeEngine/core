package de.cubeisland.engine.roles.role;

import java.util.ArrayList;
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

    private Map<String, ResolvedPermission> resolvedPermissions;
    private Map<String, ResolvedMetadata> resolvedMetadata;
    private TreeSet<Role> resolvedRoles;

    protected Set<Role> dependentRoles;

    protected RoleProvider provider;

    private boolean dirty = true;

    public boolean isDirty()
    {
        return dirty;
    }

    protected void makeDirty()
    {
        this.dirty = true;
        for (Role role : this.dependentRoles)
        {
            role.makeDirty();
        }
    }

    protected ResolvedDataHolder(RoleProvider provider)
    {
        this.provider = provider;
        this.module = provider.module;
    }

    @Override
    public void calculate(Stack<String> roleStack)
    {
        if (this.isDirty())
        {
            return;
        }
        if (roleStack.contains(this.getName()))
        {
            throw new CircularRoleDependencyException("Cannot load role! Circular Dependency detected in " + this.getName() + "\n" + StringUtils.implode(", ", roleStack));
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
        this.resolvedMetadata = new THashMap<>();
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
                    result.add(role);
                }
                catch (CircularRoleDependencyException e)
                {
                    this.module.getLog().warn(e, "A CircularRoleDependencyException occurred");
                }
            }
        }
        this.inheritRoles(result);
    }

    private void inheritRoles(TreeSet<Role> roles)
    {
        this.resolvedRoles = roles;
        this.dependentRoles = new HashSet<>();
        for (Role role : roles)
        {
            if (role.isDirty())
            {
                this.module.getLog().debug("Role to assign is dirty! {}", role.getName());
            }
            role.dependentRoles.add(role);
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
}
