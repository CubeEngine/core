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
package de.cubeisland.engine.log.action.logaction;


import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.LogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

public class ActionTypeContainer extends LogActionType
{
    private final String name;
    public ActionTypeContainer(String name)
    {
        this.setModel(null);
        this.name = name;
    }

    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive(World world)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean needsModel()
    {
        return false;
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
