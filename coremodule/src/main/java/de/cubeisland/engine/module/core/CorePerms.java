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
package de.cubeisland.engine.module.core;

import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.service.permission.PermissionContainer;
import org.spongepowered.api.service.permission.PermissionDescription;

@SuppressWarnings("all")
public class CorePerms extends PermissionContainer<CoreModule>
{
    public CorePerms(CoreModule module)
    {
        super(module);
    }

    private final PermissionDescription COMMAND = register("command", "Base Commands Permission", null);
    public final PermissionDescription COMMAND_OP_NOTIFY = register("op.notify", "Shows notifications when op-Command is used", COMMAND);

    public final PermissionDescription COMMAND_DEOP_NOTIFY = register("deop.notify", "Shows notifications when deop-Command is used", COMMAND);
    public final PermissionDescription COMMAND_DEOP_OTHER = register("deop.other", "Allow using deop on other players", COMMAND); // TODO PermDefaults?

    public final PermissionDescription COMMAND_RELOAD_NOTIFY = register("reload.notify", "Shows notifications when reloading the server", COMMAND);

    public final PermissionDescription SPAM = register("spam", "Prevents getting kicked for the Vanilla Spam Reason", null);
}
