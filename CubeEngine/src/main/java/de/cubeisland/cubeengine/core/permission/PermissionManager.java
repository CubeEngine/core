package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 * Registrates Permissions to the server.
 */
public class PermissionManager
{
    private static final String                                  CUBEENGINE_BASE     = "cubeengine";
    private static final org.bukkit.permissions.Permission       CUBEENGINE_WILDCARD = new org.bukkit.permissions.Permission(CUBEENGINE_BASE + ".*", PermissionDefault.OP);

    private final PluginManager                                  pm;
    private final Map<String, org.bukkit.permissions.Permission> wildcards;
    private final Map<Module, Set<String>>                       modulePermissionMap;
    private final Thread                                         mainThread;

    public PermissionManager(Core core)
    {
        this.pm = ((BukkitCore)core).getServer().getPluginManager();
        this.wildcards = new THashMap<String, org.bukkit.permissions.Permission>(0);
        this.modulePermissionMap = new THashMap<Module, Set<String>>(0);
        this.mainThread = Thread.currentThread();

        this.registerBukkitPermission(CUBEENGINE_WILDCARD);
    }

    private void registerBukkitPermission(org.bukkit.permissions.Permission permission)
    {
        try
        {
            this.pm.addPermission(permission);
            if (permission.getName().endsWith("*"))
            {
                this.wildcards.put(permission.getName(), permission);
            }
        }
        catch (IllegalArgumentException ignored)
        {}
    }

    /**
     * Registers a String as a permission
     *
     * @param perm        the permission node
     * @param permDefault the default valueW
     */
    public void registerPermission(Module module, String perm, PermissionDefault permDefault)
    {
        if (Thread.currentThread() != this.mainThread)
        {
            throw new IllegalStateException("Permissions may only be registered from the main thread!");
        }
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(perm, "The permission must not be null!");
        Validate.notNull(permDefault, "The permission default must not be null!");

        if (perm.equals(CUBEENGINE_WILDCARD.getName()))
        {
            return;
        }

        perm = perm.toLowerCase(Locale.ENGLISH);
        String[] parts = StringUtils.explode(".", perm);
        if (parts.length < 3 || !"cubeengine".equals(parts[0]) || !module.getId().equals(parts[1]))
        {
            throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
        }

        Set<String> modulePermissions = this.getPermission(module);
        modulePermissions.add(perm);

        org.bukkit.permissions.Permission permission = new org.bukkit.permissions.Permission(perm, permDefault);
        this.registerBukkitPermission(permission);

        org.bukkit.permissions.Permission parent = CUBEENGINE_WILDCARD;
        org.bukkit.permissions.Permission current;
        String currentString = CUBEENGINE_BASE;
        for (int i = 1; i < parts.length - 1; ++i)
        {
            currentString += "." + parts[i];
            current = this.getWildcard(module, currentString);
            current.addParent(parent, true);
            parent = current;
        }

        permission.addParent(parent, true);
    }

    private org.bukkit.permissions.Permission getWildcard(Module module, String perm)
    {
        perm += ".*";

        org.bukkit.permissions.Permission wildcard = this.wildcards.get(perm);
        if (wildcard == null)
        {
            this.registerBukkitPermission(wildcard = new org.bukkit.permissions.Permission(perm, PermissionDefault.OP));
            this.getPermission(module).add(perm);
        }

        return wildcard;
    }

    private Set<String> getPermission(Module module)
    {
        Set<String> perms = this.modulePermissionMap.get(module);
        if (perms == null)
        {
            this.modulePermissionMap.put(module, perms = new THashSet<String>(1));
        }
        return perms;
    }

    /**
     * Registeres a permission
     *
     * @param permission the permission
     */
    public void registerPermission(Module module, Permission permission)
    {
        this.registerPermission(module, permission.getPermission(), permission.getPermissionDefault());
    }

    /**
     * Registered an array of permissions
     *
     * @param permissions the array of permissions
     */
    public void registerPermissions(Module module, Permission[] permissions)
    {
        for (Permission permission : permissions)
        {
            this.registerPermission(module, permission);
        }
    }

    public void unregisterPermission(Module module, String perm)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(perm, "The permission must not be null!");
        Validate.isTrue(!perm.equals(CUBEENGINE_WILDCARD.getName()), "The CubeEngine wildcard permission must not be unregistered!");

        Set<String> perms = this.modulePermissionMap.get(module);
        if (perms != null && perms.remove(perm))
        {
            this.pm.removePermission(perm);
            if (perm.endsWith("*"))
            {
                this.wildcards.remove(perm);
            }
        }
    }

    /**
     * Unregisters all the permissions of the given module
     *
     * @param module the module
     */
    public void unregisterPermissions(Module module)
    {
        Validate.notNull(module, "The module must not be null!");

        Set<String> removedPerms = this.modulePermissionMap.remove(module);
        if (removedPerms != null)
        {
            for (String perm : removedPerms)
            {
                this.pm.removePermission(perm);
                if (perm.endsWith("*"))
                {
                    this.wildcards.remove(perm);
                }
            }
        }
    }
}
