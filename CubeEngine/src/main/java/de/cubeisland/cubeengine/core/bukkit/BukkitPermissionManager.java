package de.cubeisland.cubeengine.core.bukkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;
import static de.cubeisland.cubeengine.core.permission.Permission.BASE;

public class BukkitPermissionManager implements PermissionManager
{
    private static final org.bukkit.permissions.Permission CUBEENGINE_WILDCARD = new org.bukkit.permissions.Permission(BASE + ".*", PermissionDefault.FALSE);
    private final PluginManager pm;
    private final Map<String, org.bukkit.permissions.Permission> wildcards;
    private final Map<Module, Set<String>> modulePermissionMap;
    private final Logger logger;

    public BukkitPermissionManager(BukkitCore core)
    {
        this.pm = core.getServer().getPluginManager();
        this.wildcards = new THashMap<String, org.bukkit.permissions.Permission>(0);
        this.modulePermissionMap = new THashMap<Module, Set<String>>(0);
        this.logger = new CubeLogger("permissions");
        try
        {
            FileHandler handler = new CubeFileHandler(Level.ALL, new File(core.getFileManager().getLogDir(), this.logger.getName()).getPath());
            handler.setFormatter(new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record)
                {
                    StringBuilder sb = new StringBuilder(this.dateFormat.format(new Date(record.getMillis())));
                    sb.append(' ').append(record.getMessage()).append('\n');
                    return sb.toString();
                }
            });
            this.logger.addHandler(handler);
            this.logger.setLevel(core.getCoreLogger().getLevel());
        }
        catch (IOException e)
        {
            core.getCoreLogger().log(ERROR, "Failed to create the permission log");
        }

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
            this.logger.log(INFO, "successful " + permission.getName());
        }
        catch (IllegalArgumentException ignored)
        {
            this.logger.log(DEBUG, "duplicated " + permission.getName());
        }
    }

    private org.bukkit.permissions.Permission registerWildcard(Module module, String perm)
    {
        perm += ".*";

        org.bukkit.permissions.Permission wildcard = this.wildcards.get(perm);
        if (wildcard == null)
        {
            this.registerBukkitPermission(wildcard = new org.bukkit.permissions.Permission(perm, PermissionDefault.FALSE));
            this.getPermissions(module).add(perm);
        }

        return wildcard;
    }

    private Set<String> getPermissions(Module module)
    {
        Set<String> perms = this.modulePermissionMap.get(module);
        if (perms == null)
        {
            this.modulePermissionMap.put(module, perms = new THashSet<String>(1));
        }
        return perms;
    }

    @Override
    public org.bukkit.permissions.Permission registerPermission(Module module, String perm, PermDefault permDefault, String parent, Set<String> bundles)
    {
        if (!CubeEngine.isMainThread())
        {
            throw new IllegalStateException("Permissions may only be registered from the main thread!");
        }
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(perm, "The permission must not be null!");
        Validate.notNull(permDefault, "The permission default must not be null!");

        if (perm.equals(CUBEENGINE_WILDCARD.getName()))
        {
            return null;
        }

        perm = perm.toLowerCase(Locale.ENGLISH);
        String[] parts = StringUtils.explode(".", perm);
        if (parts.length < 3 || !BASE.equals(parts[0]) || !module.getId().equals(parts[1]))
        {
            throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
        }

        Set<String> modulePermissions = this.getPermissions(module);
        modulePermissions.add(perm);

        org.bukkit.permissions.Permission permission = this.pm.getPermission(perm);
        if (permission == null)
        {
            permission = new org.bukkit.permissions.Permission(perm, permDefault.getValue());
            this.registerBukkitPermission(permission);
        }
        else
        {
            //this will happen to all bundel-permissions
            System.out.print(permission.getName() + " already created!");//TODO remove this debug
        }
        if (parent != null)
        {
            org.bukkit.permissions.Permission parentPerm = this.registerWildcard(module, parent);
            permission.addParent(parentPerm, true);
        }
        if (bundles != null) // register the known bundles
        {
            for (String bundle : bundles)
            {
                org.bukkit.permissions.Permission bundlePerm = this.pm.getPermission(bundle);
                if (bundlePerm == null)
                {
                    bundlePerm = new org.bukkit.permissions.Permission(bundle, PermissionDefault.FALSE);
                    this.registerBukkitPermission(bundlePerm);
                }
                modulePermissions.add(bundle);
                permission.addParent(bundlePerm, true);
            }
        }
        return permission;
    }

    @Override
    public void registerPermission(Module module, Permission permission)
    {
        String parent = null;
        if (permission.hasParent())
        {
            parent = permission.getParent().getPermission();
        }
        Set<String> bundles = new THashSet<String>();
        if (permission.hasBundles())
        {
            for (Permission bundle : permission.getBundles())
            {
                bundles.add(bundle.getPermission());
            }
        }
        org.bukkit.permissions.Permission registeredPerm =
            this.registerPermission(module, permission.getPermission(), permission.getPermissionDefault(),
                                    parent, bundles);
        Permission parentpermission;
        while (permission.hasParent())
        {
            parentpermission = permission.getParent();
            if (parentpermission.canRegister)
            {
                return;
            }
            org.bukkit.permissions.Permission parentPerm = this.registerWildcard(module, parentpermission.getPermission());
            registeredPerm.addParent(parentPerm,true);
            // next perm above
            registeredPerm = parentPerm;
            permission = parentpermission;
        }
    }

    @Override
    public void registerPermissions(Module module, Permission[] permissions)
    {
        for (Permission permission : permissions)
        {
            this.registerPermission(module, permission);
        }
    }

    public void removePermission(Module module, String perm)
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

    public void removePermissions(Module module)
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

    public void removePermissions()
    {
        Iterator<Entry<Module, Set<String>>> modulesIter = this.modulePermissionMap.entrySet().iterator();
        Entry<Module, Set<String>> entry;

        while (modulesIter.hasNext())
        {
            entry = modulesIter.next();
            modulesIter.remove();
            for (String perm : entry.getValue())
            {
                this.pm.removePermission(perm);
            }
        }
    }

    public PermDefault getDefaultFor(String permission)
    {
        if (permission == null)
        {
            throw new NullPointerException("The permission must not be null!");
        }
        org.bukkit.permissions.Permission perm = this.pm.getPermission(permission);
        if (perm == null)
        {
            return null;
        }
        switch (perm.getDefault())
        {
            case TRUE:
                return PermDefault.TRUE;
            case FALSE:
                return PermDefault.FALSE;
            case OP:
                return PermDefault.OP;
            case NOT_OP:
                return PermDefault.NOT_OP;
            default:
                return null;
        }
    }

    public void clean()
    {
        this.removePermissions();
        this.wildcards.clear();
        this.modulePermissionMap.clear();
    }
}
