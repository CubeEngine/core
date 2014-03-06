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
package de.cubeisland.engine.log.action.logaction.block.interaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * Igniting Tnt with a lighter
 * <p>Events: {@link RightClickActionType}</p>
 */
public class TntPrime extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(EXPLOSION, BLOCK, PLAYER));
    }

    @Override
    public String getName()
    {
        return "tnt-prime";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated(MessageType.POSITIVE, "{}{user} ignited {amount} TNT!{}", time, logEntry.getCauserUser().getDisplayName(), amount, loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} ignited TNT!{}", time, logEntry.getCauserUser().getDisplayName(), loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.TNT_PRIME_enable;
    }
}
