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

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;
import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.logger.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.permission.Permission.BASE;

public class BukkitPermissionManager implements PermissionManager
{
    private static final org.bukkit.permissions.Permission CUBEENGINE_WILDCARD = new org.bukkit.permissions.Permission(BASE + ".*", PermissionDefault.FALSE);
    private final PluginManager pm;
    private final Map<String, Permission> wildcards;
    private final Map<Module, Set<String>> modulePermissionMap;
    private final Logger logger;

    public BukkitPermissionManager(BukkitCore core)
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

    public void registerPermission(Module module, String perm, PermDefault permDefault)
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

        Set<String> modulePermissions = this.getPermission(module);
        modulePermissions.add(perm);

        org.bukkit.permissions.Permission permission = new org.bukkit.permissions.Permission(perm, permDefault.getValue());
        this.registerBukkitPermission(permission);

        org.bukkit.permissions.Permission parent = CUBEENGINE_WILDCARD;
        org.bukkit.permissions.Permission current;
        String currentString = BASE;
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
            this.registerBukkitPermission(wildcard = new org.bukkit.permissions.Permission(perm, PermissionDefault.FALSE));
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

    public void registerPermission(Module module, de.cubeisland.cubeengine.core.permission.Permission permission)
    {
        this.registerPermission(module, permission.getPermission(), permission.getPermissionDefault());
    }

    public void registerPermissions(Module module, de.cubeisland.cubeengine.core.permission.Permission[] permissions)
    {
        for (de.cubeisland.cubeengine.core.permission.Permission permission : permissions)
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
        Iterator<Map.Entry<Module, Set<String>>> modulesIter = this.modulePermissionMap.entrySet().iterator();
        Map.Entry<Module, Set<String>> entry;

        while (modulesIter.hasNext())
        {
            entry = modulesIter.next();
            modulesIter.remove();
            for (String perm : entry.getValue())
            {
                this.pm.removePermission(perm);
            }
        }

        Iterator<Entry<String, Permission>> wildcardsIter = this.wildcards.entrySet().iterator();
        while (wildcardsIter.hasNext())
        {
            this.pm.removePermission(wildcardsIter.next().getKey());
            wildcardsIter.remove();
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
