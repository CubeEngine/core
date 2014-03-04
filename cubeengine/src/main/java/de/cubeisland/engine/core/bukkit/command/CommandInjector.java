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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.VanillaCommand;

import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitCoreConfiguration;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.module.Module;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.contract.Contract.expect;
import static de.cubeisland.engine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.engine.core.util.ReflectionUtils.getFieldValue;

public class CommandInjector
{
    protected final BukkitCore core;
    private final Field commandMapField;
    private SimpleCommandMap commandMap;
    private Field knownCommandField;

    public CommandInjector(BukkitCore core)
    {
        this.core = core;
        this.commandMapField = findFirstField(core.getServer(), SimpleCommandMap.class);
    }

    @SuppressWarnings("unchecked")
    protected synchronized Map<String, Command> getKnownCommands()
    {
        return getFieldValue(getCommandMap(), knownCommandField, Map.class);
    }

    protected final SimpleCommandMap getCommandMap()
    {
        SimpleCommandMap map = getFieldValue(this.core.getServer(), commandMapField, SimpleCommandMap.class);
        if (this.commandMap != map)
        {
            this.knownCommandField = findFirstField(map, Map.class, 1);
        }
        this.commandMap = map;
        return map;
    }

    public synchronized void registerCommand(CubeCommand command)
    {
        expectNotNull(command.getDescription(), command.getName() + " doesn't have a description!");
        expect(!command.getDescription().isEmpty(), command.getName() + " has an empty description!");

        SimpleCommandMap commandMap = getCommandMap();
        Command old = this.getCommand(command.getName());
        if (old != null && !(old instanceof CubeCommand))
        {
            BukkitCoreConfiguration config = this.core.getConfiguration();
            if (!config.commands.noOverride.contains(old.getLabel().toLowerCase(Locale.ENGLISH)))
            {
                // CE commands are more important :P
                this.removeCommand(old.getLabel(), false);
                String fallbackPrefix = core.getConfiguration().defaultFallback;
                if (old instanceof PluginCommand)
                {
                    fallbackPrefix = ((PluginCommand)old).getPlugin().getName();
                }
                else if (old instanceof VanillaCommand)
                {
                    fallbackPrefix = "vanilla";
                }
                getKnownCommands().put(fallbackPrefix + ":" + command.getLabel(), command);
                command.register(commandMap);
            }// sometimes they are not :(
        }

        commandMap.register(command.getModule().getId(), command);
        command.onRegister();
    }

    public Command getCommand(String name)
    {
        return getCommandMap().getCommand(name);
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        return getCommandMap().dispatch(sender, commandLine);
    }

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
                removed.unregister(getCommandMap());
                if (removed instanceof CubeCommand)
                {
                    ((CubeCommand)removed).onRemove();
                }
            }
        }
    }

    public void removeCommands(Module module)
    {
        CubeCommand cubeCommand;
        for (Command command : new THashSet<>(getCommandMap().getCommands()))
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

    public void removeCommands()
    {
        for (Command command : new THashSet<>(getCommandMap().getCommands()))
        {
            if (command instanceof CubeCommand)
            {
                this.removeCommand(command.getLabel(), true);
            }
        }
    }

    public void shutdown()
    {
        this.removeCommands();
        this.commandMap = null;
        this.knownCommandField = null;
    }
}
