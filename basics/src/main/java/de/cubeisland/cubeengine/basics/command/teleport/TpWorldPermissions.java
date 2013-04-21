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
package de.cubeisland.cubeengine.basics.command.teleport;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.basics.BasicsPerm.COMMAND;

/**
 * Dynamically registered Permissions for each world.
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
