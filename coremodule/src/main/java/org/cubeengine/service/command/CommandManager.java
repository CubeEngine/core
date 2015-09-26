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
package org.cubeengine.service.command;

import org.cubeengine.butler.CommandBuilder;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.parametric.BasicParametricCommand;
import org.cubeengine.butler.ProviderManager;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.module.core.util.Cleanable;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.ConsoleSource;

/**
 * This class manages the registration of commands.
 */
@Service
@Version(1)
public interface CommandManager extends Cleanable, Dispatcher
{
    ProviderManager getProviderManager();

    /**
     * Removes a command by its name
     *
     * @param name       the name of the command to remove
     * @param completely whether to remove all the aliases as well
     */
    void removeCommand(String name, boolean completely);

    /**
     * Removes all commands of a module
     *
     * @param module the module
     */
    void removeCommands(Module module);

    /**
     * Removes all commands of the CubeEngine
     */
    void removeCommands();

    boolean runCommand(CommandSource sender, String commandLine);

    ConsoleSource getConsoleSender();

    void logExecution(CommandSource sender, boolean ran, String alias, String args);

    void logTabCompletion(CommandSource sender, String alias, String args);

    CommandBuilder<BasicParametricCommand, CommandOrigin> getCommandBuilder();


    /**
     * Creates {@link org.cubeengine.butler.parametric.BasicParametricCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param module        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    void addCommands(Dispatcher dispatcher, Module module, Object commandHolder);
    void addCommands(Module module, Object commandHolder);

    void logCommands(boolean logCommands);
}
