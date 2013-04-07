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
package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredPrevention;
import gnu.trove.set.hash.THashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Prevents command usage.
 */
public class CommandPrevention extends FilteredPrevention<String>
{
    public CommandPrevention(Guests guests)
    {
        super("command", guests);
        setEnableByDefault(true);
        setEnablePunishing(true);
        setFilterItems(new THashSet<String>(Arrays.asList("guestss", "pl", "version")));
    }

    @Override
    public Set<String> decodeList(List<String> list)
    {
        Set<String> commands = new THashSet<String>(list.size());
        for (String entry : list)
        {
            commands.add(entry.trim().toLowerCase());
        }
        return commands;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void commandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        if (message.startsWith("/"))
        {
            message = message.substring(1).trim();
            final int spaceIndex = message.indexOf(' ');
            if (spaceIndex >= 0)
            {
                message = message.substring(0, spaceIndex);
            }
            if (message.length() > 0)
            {
                prevent(event, event.getPlayer(), message);
            }
        }
    }
}
