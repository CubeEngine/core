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
import java.util.concurrent.TimeUnit;

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;


/**
 * Changing NoteblockNotes
 * <p>Events: {@link RightClickActionType}</p>
 */
public class NoteBlockChange extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, PLAYER));
    }

    @Override
    public String getName()
    {
        return "noteblock-change";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        Long oldClicks = logEntry.getData();
        Integer newClicks = logEntry.getNewdata().intValue();
        if (logEntry.hasAttached())
        {
            LogEntry last = logEntry.getAttached().last();
            newClicks = last.getNewdata().intValue();
        }
        if (oldClicks.intValue() == newClicks)
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} fiddled around with the noteblock but did not change anything{}", time, logEntry.getCauserUser().getDisplayName(), loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} set the noteblock to {amount} clicks{}", time, logEntry.getCauserUser().getDisplayName(), newClicks, loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.NOTEBLOCK_CHANGE_enable;
    }

    @Override
    protected boolean nearTimeFrame(LogEntry logEntry, LogEntry other)
    {
        return Math.abs(TimeUnit.MILLISECONDS.toMinutes(logEntry.getTimestamp().getTime() - other.getTimestamp().getTime())) < 2;
    }
}
