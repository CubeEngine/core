package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.Permissible;

import java.util.Map;

/**
 * Dynamicly registerd Permissions for each world.
 */
public class TpWorldPermissions
{
    private static Map<String, Permission> permissions = new THashMap<String, Permission>();

    public Permission[] getPermissions()
    {
        return permissions.values().toArray(new Permission[0]);
    }

    public static Permission getPermission(String world)
    {
        return permissions.get(world);
    }

    public TpWorldPermissions(Basics basics)
    {
        for (final World world : Bukkit.getWorlds())
        {
            permissions.put(world.getName(), new Permission()
            {
                private String permission = "cubeengine.basics.command.tpworld." + world.getName();

                @Override
                public boolean isAuthorized(Permissible player)
                {
                    return player.hasPermission(permission);
                }

                @Override
                public String getPermission()
                {
                    return this.permission;
                }

                @Override
                public PermDefault getPermissionDefault()
                {
                    return PermDefault.OP;
                }
            });
        }
    }
}
