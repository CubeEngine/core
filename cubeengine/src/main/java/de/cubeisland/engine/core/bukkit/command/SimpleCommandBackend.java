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
package de.cubeisland.engine.core.bukkit.command;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.ReflectionUtils;

import gnu.trove.set.hash.THashSet;

public class SimpleCommandBackend implements CommandBackend
{
    protected final BukkitCore core;
    private final SimpleCommandMap commandMap;
    private Map<String, Command> knownCommands;

    public SimpleCommandBackend(BukkitCore core, SimpleCommandMap commandMap)
    {
        this.core = core;
        this.commandMap = commandMap;
        this.knownCommands = null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Command> getKnownCommands()
    {
        if (this.knownCommands == null)
        {
            this.knownCommands = ReflectionUtils.getFieldValue(commandMap, ReflectionUtils.findFirstField(commandMap, Map.class, 1), Map.class);
        }
        return this.knownCommands;
    }

    protected final SimpleCommandMap getCommandMap()
    {
        return this.commandMap;
    }

    @Override
    public void registerCommand(CubeCommand command)
    {
        assert command.getDescription() != null && !command.getDescription().isEmpty(): command.getName() + " doesn't have a description!";

        Command old = this.getCommand(command.getName());
        if (old != null && !(old instanceof CubeCommand))
        {
            // CE commands are more important :P
            this.removeCommand(old.getLabel(), false);
        }

        this.commandMap.register(command.getModule().getId(), command);
        command.onRegister();
    }

    @Override
    public Command getCommand(String name)
    {
        return this.commandMap.getCommand(name);
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        return this.commandMap.dispatch(sender, commandLine);
    }

    @Override
    public void removeCommand(String name, boolean completely)
    {
        Map<String, Command> knownCommands = this.getKnownCommands();
        Command removed = knownCommands.remove(name.toLowerCase());
        if (removed != null)
        {
            Iterator<Entry<String, Command>> it = knownCommands.entrySet().iterator();
            Command next;
            boolean hasAliases = false;
            while (it.hasNext())
            {
                next = it.next().getValue();
                if (next == removed)
                {
                    hasAliases = true;
                    if (completely)
                    {
                        it.remove();
                    }
                }
            }
            if (hasAliases)
            {
                removed.unregister(this.commandMap);
                if (removed instanceof CubeCommand)
                {
                    ((CubeCommand)removed).onRemove();
                }
            }
        }
    }

    @Override
    public void removeCommands(Module module)
    {
        CubeCommand cubeCommand;
        for (Command command : new THashSet<>(this.commandMap.getCommands()))
        {
            if (command instanceof CubeCommand)
            {
                cubeCommand = (CubeCommand)command;
                if (cubeCommand.getModule() == module)
                {
                    this.removeCommand(cubeCommand.getLabel(), true);
                }
                else
                {
                    this.removeSubCommands(module, cubeCommand);
                }
            }
        }
    }

    private void removeSubCommands(Module module, CubeCommand command)
    {
        if (!command.hasChildren())
        {
            return;
        }
        Iterator<CubeCommand> it = command.getChildren().iterator();
        CubeCommand child;
        while (it.hasNext())
        {
            child = it.next();
            if (child.getModule() == module)
            {
                child.onRemove();
                it.remove();
            }
            else
            {
                this.removeSubCommands(module, child);
            }
        }
    }

    @Override
    public void removeCommands()
    {
        for (Command command : new THashSet<>(this.commandMap.getCommands()))
        {
            if (command instanceof CubeCommand)
            {
                this.removeCommand(command.getLabel(), true);
            }
        }
    }

    @Override
    public void shutdown()
    {
        this.removeCommands();
        this.knownCommands = null;
    }
}
