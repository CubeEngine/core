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
package de.cubeisland.engine.fun;

import java.util.Locale;

import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.permission.WildcardPermission;

public class FunPerm extends PermissionContainer<Fun>
{
    public static boolean ARE_THROW_ITEMS_REGISTERED = false;

    public FunPerm(Fun module)
    {
        this.registerAllPermissions(module);

        if (!ARE_THROW_ITEMS_REGISTERED)
        {
            PermissionManager perm = module.getCore().getPermissionManager();
            for (EntityType type : EntityType.values())
            {
                if (type.isSpawnable())
                {
                    perm.registerPermission(module, FunPerm.COMMAND_THROW.child(type.name().toLowerCase(Locale.ENGLISH).replace("_", "-")));
                }
            }
            ARE_THROW_ITEMS_REGISTERED = true;
        }
    }

    private static final WildcardPermission COMMAND = Permission.createWildcard("command");

    private static final WildcardPermission COMMAND_EXPLOSION = COMMAND.childWildcard("explosion");
    public static final Permission COMMAND_EXPLOSION_OTHER = COMMAND_EXPLOSION.child("other");
    public static final Permission COMMAND_EXPLOSION_PLAYER_DAMAGE = COMMAND_EXPLOSION.child("player.damage");
    public static final Permission COMMAND_EXPLOSION_BLOCK_DAMAGE = COMMAND_EXPLOSION.child("block.damage");
    public static final Permission COMMAND_EXPLOSION_FIRE = COMMAND_EXPLOSION.child("fire");

    private static final WildcardPermission COMMAND_HAT = COMMAND.childWildcard("hat");
    public static final Permission COMMAND_HAT_OTHER = COMMAND_HAT.child("other");
    public static final Permission COMMAND_HAT_ITEM = COMMAND_HAT.child("item");
    public static final Permission COMMAND_HAT_QUIET = COMMAND_HAT.child("quit");
    public static final Permission COMMAND_HAT_NOTIFY = COMMAND_HAT.child("notify", PermDefault.TRUE);

    private static final WildcardPermission COMMAND_LIGHTNING = COMMAND.childWildcard("lightning");
    public static final Permission COMMAND_LIGHTNING_PLAYER_DAMAGE = COMMAND_LIGHTNING.child("player.damage");
    public static final Permission COMMAND_LIGHTNING_UNSAFE = COMMAND_LIGHTNING.child("unsafe");

    public static final WildcardPermission COMMAND_THROW = COMMAND.childWildcard("throw");
    public static final Permission COMMAND_THROW_UNSAFE = COMMAND_THROW.child("unsafe");

    private static final WildcardPermission COMMAND_NUKE = COMMAND.childWildcard("nuke");
    public static final Permission COMMAND_NUKE_CHANGE_RANGE = COMMAND_NUKE.child("change_range");
    public static final Permission COMMAND_NUKE_OTHER = COMMAND_NUKE.child("other");
}
