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
package de.cubeisland.cubeengine.core.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.craftbukkit.libs.jline.console.completer.Completer;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.util.StringUtils;

public class ConsoleCommandCompleter implements Completer
{
    private final Server server;
    private final CommandMap commandMap;

    public ConsoleCommandCompleter(BukkitCore core)
    {
        this.server = core.getServer();
        this.commandMap = BukkitUtils.getCommandMap(this.server);
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates)
    {
        Collection <String> bukkitCandidates = this.commandMap.tabComplete(this.server.getConsoleSender(), buffer);
        if (bukkitCandidates == null)
        {
            String token = StringUtils.getLastPart(buffer, " ");
            Player[] onlinePlayers = this.server.getOnlinePlayers();
            bukkitCandidates = new ArrayList<String>(onlinePlayers.length);
            for (Player player : onlinePlayers)
            {
                if (StringUtils.endsWithIgnoreCase(player.getName(), token))
                {
                    bukkitCandidates.add(player.getName());
                }
            }
        }

        int cursorDiff;
        if (buffer.indexOf(' ') == -1)
        {
            cursorDiff = buffer.length();
            for (String entry : bukkitCandidates)
            {
                candidates.add(entry.substring(1));
            }
        }
        else
        {
            cursorDiff = buffer.length() - buffer.lastIndexOf(' ') - 1;
            candidates.addAll(bukkitCandidates);
        }

        return cursor - cursorDiff;
    }
}
