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
package de.cubeisland.cubeengine.core.bukkit.command;

import java.lang.reflect.Field;
import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CubeCommand;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;
import static de.cubeisland.cubeengine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.cubeengine.core.util.ReflectionUtils.getFieldValue;

public class CubeCommandBackend extends SimpleCommandBackend
{
    public CubeCommandBackend(BukkitCore core)
    {
        super(core, swapCommandMap(core, null));
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        this.resetCommandMap();
    }

    private static SimpleCommandMap swapCommandMap(BukkitCore core, SimpleCommandMap newCommandMAp)
    {
        final Server server = core.getServer();
        final PluginManager pm = server.getPluginManager();
        final Field serverField = findFirstField(server, CommandMap.class);
        final Field pmField = findFirstField(pm, CommandMap.class);

        if (newCommandMAp == null)
        {
            newCommandMAp = new CubeCommandMap(core, getFieldValue(server, serverField, SimpleCommandMap.class));
        }
        if (serverField != null && pmField != null)
        {
            try
            {
                serverField.set(server, newCommandMAp);
                pmField.set(pm, newCommandMAp);
            }
            catch (Exception e)
            {
                CubeEngine.getLog().log(DEBUG, e.getLocalizedMessage(), e);
            }
        }
        return newCommandMAp;
    }

    private void resetCommandMap()
    {
        SimpleCommandMap current = getFieldValue(core.getServer(), findFirstField(this.core.getServer(), CommandMap.class), SimpleCommandMap.class);
        if (current != null && current instanceof CubeCommandMap)
        {
            CubeCommandMap cubeMap = (CubeCommandMap)current;
            swapCommandMap(this.core, current = new SimpleCommandMap(this.core.getServer()));

            Collection<Command> commands = cubeMap.getCommands();

            for (Command command : commands)
            {
                command.unregister(cubeMap);
                if (command instanceof CubeCommand)
                {
                    continue;
                }
                String prefix = "";
                if (command instanceof PluginCommand)
                {
                    prefix = ((PluginCommand)command).getPlugin().getName();
                }
                else if (command instanceof BukkitCommand)
                {
                    prefix = "bukkit";
                }
                current.register(command.getLabel(), prefix, command);
            }
        }
    }
}
