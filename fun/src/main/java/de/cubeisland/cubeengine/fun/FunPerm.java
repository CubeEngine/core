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
package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

public class FunPerm extends PermissionContainer<Fun>
{
    public FunPerm(Fun module)
    {
        super(module);
        this.bindToModule(COMMAND);
        this.registerAllPermissions();
    }

    private static final Permission COMMAND = Permission.createAbstractPermission("command");

    private static final Permission COMMAND_EXPLOSION = COMMAND.createAbstractChild("explosion");
    public static final Permission COMMAND_EXPLOSION_OTHER = COMMAND_EXPLOSION.createChild("other");
    public static final Permission COMMAND_EXPLOSION_PLAYER_DAMAGE = COMMAND_EXPLOSION.createChild("player.damage");
    public static final Permission COMMAND_EXPLOSION_BLOCK_DAMAGE = COMMAND_EXPLOSION.createChild("block.damage");
    public static final Permission COMMAND_EXPLOSION_FIRE = COMMAND_EXPLOSION.createChild("fire");

    private static final Permission COMMAND_HAT = COMMAND.createAbstractChild("hat");
    public static final Permission COMMAND_HAT_OTHER = COMMAND_HAT.createChild("other");
    public static final Permission COMMAND_HAT_ITEM = COMMAND_HAT.createChild("item");
    public static final Permission COMMAND_HAT_QUIET = COMMAND_HAT.createChild("quit");
    public static final Permission COMMAND_HAT_NOTIFY = COMMAND_HAT.createChild("notify",PermDefault.TRUE);

    private static final Permission COMMAND_LIGHTNING = COMMAND.createAbstractChild("lightning");
    public static final Permission COMMAND_LIGHTNING_PLAYER_DAMAGE = COMMAND_LIGHTNING.createChild("player.damage");
    public static final Permission COMMAND_LIGHTNING_UNSAFE = COMMAND_LIGHTNING.createChild("unsafe");

    public static final Permission COMMAND_THROW = COMMAND.createAbstractChild("throw");
    public static final Permission COMMAND_THROW_UNSAFE = COMMAND_THROW.createChild("unsafe");
}
