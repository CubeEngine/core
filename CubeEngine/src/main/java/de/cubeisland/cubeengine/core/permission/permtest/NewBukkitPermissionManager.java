package de.cubeisland.cubeengine.core.permission.permtest;

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

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.NewPermission;
import de.cubeisland.cubeengine.core.permission.NewPermissionManager;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;
import static de.cubeisland.cubeengine.core.permission.Permission.BASE;

public class NewBukkitPermissionManager implements NewPermissionManager
{
    private static final Permission CUBEENGINE_WILDCARD = new Permission(BASE + ".*", PermissionDefault.FALSE);
    private final PluginManager pm;
    private final Map<String, Permission> wildcards;
    private final Map<Module, Set<String>> modulePermissionMap;
    private final Logger logger;



    public NewBukkitPermissionManager(BukkitCore core)
    {
        this.pm = core.getServer().getPluginManager();
        this.wildcards = new THashMap<String, Permission>(0);
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

    private void registerBukkitPermission(Permission permission)
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

    private Permission registerWildcard(Module module, String perm)
    {
        perm += ".*";

        Permission wildcard = this.wildcards.get(perm);
        if (wildcard == null)
        {
            this.registerBukkitPermission(wildcard = new Permission(perm, PermissionDefault.FALSE));
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
    public void registerPermission(Module module, String perm, PermDefault permDefault, String parent, Set<String> roots)
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
            return;
        }

        perm = perm.toLowerCase(Locale.ENGLISH);
        String[] parts = StringUtils.explode(".", perm);
        if (parts.length < 3 || !BASE.equals(parts[0]) || !module.getId().equals(parts[1]))
        {
            throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
        }

        Set<String> modulePermissions = this.getPermissions(module);
        modulePermissions.add(perm);

        Permission permission = this.pm.getPermission(perm);
        if (permission == null)
        {
            permission = new Permission(perm, permDefault.getValue());
            this.registerBukkitPermission(permission);
        }
        else
        {
            //this will happen to all bundel-permissions
            System.out.print(permission.getName() + " already created!");//TODO remove this debug
        }
        if (parent != null)
        {
            Permission parentPerm = this.registerWildcard(module, parent);
            permission.addParent(parentPerm, true);
        }
        for (String root : roots)
        {
            Permission rootPerm = this.pm.getPermission(root);
            if (rootPerm == null)
            {
                rootPerm = new Permission(root, PermissionDefault.FALSE);
                this.registerBukkitPermission(rootPerm);
            }
            modulePermissions.add(root);
            permission.addParent(rootPerm, true);
        }
    }

    @Override
    public void registerPermission(Module module, NewPermission permission)
    {
        String parent = null;
        if (permission.hasParent())
        {
            parent = permission.getParent().getPermission();
        }
        Set<String> roots = new THashSet<String>();
        if (permission.hasRoots())
        {
            for (NewPermission root : permission.getRoots())
            {
                roots.add(root.getPermission());
            }
        }
        this.registerPermission(module,permission.getPermission(),permission.getPermissionDefault(),parent,roots);
    }

    @Override
    public void registerPermissions(Module module, NewPermission[] permissions)
    {
        for (NewPermission permission : permissions)
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
        Permission perm = this.pm.getPermission(permission);
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
