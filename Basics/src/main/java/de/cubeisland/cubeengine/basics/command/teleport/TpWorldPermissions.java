package de.cubeisland.cubeengine.basics.command.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.Permissible;

import java.util.Map;
import java.util.Set;

import static de.cubeisland.cubeengine.basics.BasicsPerm.COMMAND;

/**
 * Dynamicly registerd Permissions for each world.
 */
public class TpWorldPermissions extends PermissionContainer
{
    private static final Permission COMMAND_TPWORLD = COMMAND.createAbstractChild("tpworld");
    private static Map<String, Permission> permissions = new THashMap<String, Permission>();

    public TpWorldPermissions(Module module)
    {
        super(module);
        for (final World world : Bukkit.getWorlds())
        {
            permissions.put(world.getName(), COMMAND_TPWORLD.createChild(world.getName()));
        }
        this.registerAllPermissions();
    }

    @Override
    public Set<Permission> getPermissions()
    {
        return new THashSet<Permission>(permissions.values());
    }

    public static Permission getPermission(String world)
    {
        return permissions.get(world);
    }

}
