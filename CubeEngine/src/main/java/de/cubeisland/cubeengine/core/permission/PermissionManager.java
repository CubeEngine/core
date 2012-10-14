package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 * Registrates Permissions to the server
 *
 * @author Phillip Schichtel
 */
public class PermissionManager
{
    private static final String CUBEENGINE_WILDCARD = "cubeengine.*";
    
    private final PluginManager pm;
    private final Map<String, org.bukkit.permissions.Permission> wildcardPermission;
    private final Map<Module, List<String>> modulePermissionMap;
    private final Thread mainThread;

    public PermissionManager(PluginManager pm)
    {
        this.pm = pm;
        this.wildcardPermission = new THashMap<String, org.bukkit.permissions.Permission>(0);
        this.modulePermissionMap = new THashMap<Module, List<String>>(0);
        this.mainThread = Thread.currentThread();

        this.registerBukkitPermission(new org.bukkit.permissions.Permission("cubeengine.*"));
    }

    private void registerBukkitPermission(org.bukkit.permissions.Permission permission)
    {
        try
        {
            this.pm.addPermission(permission);
            if (permission.getName().endsWith("*"))
            {
                this.wildcardPermission.put(permission.getName(), permission);
            }
        }
        catch (IllegalArgumentException ignored)
        {
        }
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

        if (perm.equals(CUBEENGINE_WILDCARD))
        {
            return;
        }

        perm = perm.toLowerCase(Locale.ENGLISH);
        String[] parts = StringUtils.explode(".", perm);
        if (parts.length < 3 || !"cubeengine".equals(parts[0]) || !module.getId().equals(parts[1]))
        {
            throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
        }

        List<String> modulePermissions = this.modulePermissionMap.get(module);
        if (modulePermissions == null)
        {
            this.modulePermissionMap.put(module, modulePermissions = new ArrayList<String>(1));
        }
        modulePermissions.add(perm);
        org.bukkit.permissions.Permission permission = new org.bukkit.permissions.Permission(perm, permDefault);
        this.registerBukkitPermission(permission);

        org.bukkit.permissions.Permission oldPermission = permission;
        String base = "cubeengine.module.";

        permission = this.wildcardPermission.get(base + "*");
        if (permission == null)
        {
            permission = new org.bukkit.permissions.Permission(base + "*", PermissionDefault.FALSE);
        }
        this.registerBukkitPermission(permission);
        oldPermission.addParent(permission, true);

        for (int i = 2; i < parts.length; ++i)
        {
        }

        permission.addParent(permission, true);
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
        Validate.isTrue(!perm.equals(CUBEENGINE_WILDCARD), "The CubeEngine wildcard permission must not be unregistered!");

        List<String> perms = this.modulePermissionMap.get(module);
        if (perms != null && perms.remove(perm))
        {
            this.pm.removePermission(perm);
            if (perm.endsWith("*"))
            {
                this.wildcardPermission.remove(perm);
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
        
        List<String> removedPerms = this.modulePermissionMap.remove(module);
        if (removedPerms != null)
        {
            for (String perm : removedPerms)
            {
                this.pm.removePermission(perm);
                if (perm.endsWith("*"))
                {
                    this.wildcardPermission.remove(perm);
                }
            }
        }
    }
}