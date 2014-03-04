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

public class FunPerm extends PermissionContainer<Fun>
{
    public boolean ARE_THROW_ITEMS_REGISTERED = false;

    public FunPerm(Fun module)
    {
        super(module);
        this.registerAllPermissions();

        if (!ARE_THROW_ITEMS_REGISTERED)
        {
            PermissionManager perm = module.getCore().getPermissionManager();
            for (EntityType type : EntityType.values())
            {
                if (type.isSpawnable())
                {
                    perm.registerPermission(module, COMMAND_THROW.child(type.name().toLowerCase(Locale.ENGLISH).replace("_", "-")));
                }
            }
            ARE_THROW_ITEMS_REGISTERED = true;
        }
    }

    private final Permission COMMAND = getBasePerm().childWildcard("command");

    private final Permission COMMAND_EXPLOSION = COMMAND.childWildcard("explosion");
    public final Permission COMMAND_EXPLOSION_OTHER = COMMAND_EXPLOSION.child("other");
    public final Permission COMMAND_EXPLOSION_PLAYER_DAMAGE = COMMAND_EXPLOSION.child("player.damage");
    public final Permission COMMAND_EXPLOSION_BLOCK_DAMAGE = COMMAND_EXPLOSION.child("block.damage");
    public final Permission COMMAND_EXPLOSION_FIRE = COMMAND_EXPLOSION.child("fire");

    private final Permission COMMAND_HAT = COMMAND.childWildcard("hat");
    public final Permission COMMAND_HAT_OTHER = COMMAND_HAT.child("other");
    public final Permission COMMAND_HAT_ITEM = COMMAND_HAT.child("item");
    public final Permission COMMAND_HAT_QUIET = COMMAND_HAT.child("quit");
    public final Permission COMMAND_HAT_NOTIFY = COMMAND_HAT.child("notify", PermDefault.TRUE);

    private final Permission COMMAND_LIGHTNING = COMMAND.childWildcard("lightning");
    public final Permission COMMAND_LIGHTNING_PLAYER_DAMAGE = COMMAND_LIGHTNING.child("player.damage");
    public final Permission COMMAND_LIGHTNING_UNSAFE = COMMAND_LIGHTNING.child("unsafe");

    public final Permission COMMAND_THROW = COMMAND.childWildcard("throw");
    public final Permission COMMAND_THROW_UNSAFE = COMMAND_THROW.child("unsafe");

    private final Permission COMMAND_NUKE = COMMAND.childWildcard("nuke");
    public final Permission COMMAND_NUKE_CHANGE_RANGE = COMMAND_NUKE.child("change_range");
    public final Permission COMMAND_NUKE_OTHER = COMMAND_NUKE.child("other");
}
