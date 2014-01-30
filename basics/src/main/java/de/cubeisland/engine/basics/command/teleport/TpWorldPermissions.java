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
package de.cubeisland.engine.basics.command.teleport;

import java.util.Map;

import org.bukkit.World;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import gnu.trove.map.hash.THashMap;

/**
 * Dynamically registered Permissions for each world.
 */
public class TpWorldPermissions extends PermissionContainer<Basics>
{
    private final Permission COMMAND_TPWORLD;
    private final Map<String, Permission> permissions = new THashMap<>();

    public TpWorldPermissions(Basics module, BasicsPerm perm)
    {
        super(module);
        COMMAND_TPWORLD = perm.COMMAND.childWildcard("tpworld");
        for (final World world : module.getCore().getWorldManager().getWorlds())
        {
            initWorldPermission(world.getName());
        }
    }

    private Permission initWorldPermission(String world)
    {
        Permission perm = COMMAND_TPWORLD.child(world);
        permissions.put(world, perm);
        module.getCore().getPermissionManager().registerPermission(module,perm);
        return perm;
    }

    public Permission getPermission(String world)
    {
        Permission perm = permissions.get(world);
        if (perm == null)
        {
            perm = initWorldPermission(world);
        }
        return perm;
    }
}
