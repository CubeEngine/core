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
package de.cubeisland.engine.log.action.newaction;

import org.bukkit.World;
import org.bukkit.event.Listener;

import de.cubeisland.engine.core.module.Module;

public class LogListener implements Listener
{
    protected final Module module;

    public LogListener(Module module)
    {
        this.module = module;
    }

    protected final <T extends ActionTypeBase<?>> T newAction(Class<T> clazz, World world)
    {
        if (!this.isActive(clazz, world))
        {
            return null;
        }
        return this.newAction(clazz);
    }

    protected final <T extends ActionTypeBase<?>> T newAction(Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new IllegalArgumentException("Given LogAction cannot be instantiated!");
        }
    }

    protected final void logAction(ActionTypeBase action)
    {
        // TODO
    }

    protected final boolean isActive(Class<?> clazz, World world)
    {
        // TODO
        return true;
    }
}
