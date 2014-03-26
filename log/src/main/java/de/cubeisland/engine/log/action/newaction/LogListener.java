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

public class LogListener implements Listener
{
    public <T extends ActionTypeBase<?>> T newAction(Class<T> clazz, World world)
    {
        // TODO check if actionType is active for given world
        try
        {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
             throw new IllegalArgumentException("Given LogAction cannot be instantiated!");
        }
    }

    public void logAction(ActionTypeBase action)
    {
        // TODO
    }
}
