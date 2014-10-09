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
package de.cubeisland.engine.core.permission;

import org.bukkit.event.HandlerList;

import de.cubeisland.engine.core.bukkit.CubeEvent;
import de.cubeisland.engine.core.module.Module;

/**
 * Event gets called after a new Bukkit permission was registered by CubeEngine.
 */
public class NotifyPermissionRegistrationCompletedEvent extends CubeEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final Module module;
    private final Permission[] permissions;

    public NotifyPermissionRegistrationCompletedEvent(Module module, Permission[] permissions)
    {
        super(module.getCore());

        this.module = module;
        this.permissions = permissions;
    }

    public Module getModule()
    {
        return module;
    }

    public Permission[] getPermissions()
    {
        return permissions;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
