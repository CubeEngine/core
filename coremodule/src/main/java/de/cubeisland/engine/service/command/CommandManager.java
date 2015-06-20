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
package de.cubeisland.engine.service.command;

import de.cubeisland.engine.butler.CommandBuilder;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.Dispatcher;
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.ProviderManager;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.service.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.module.core.util.Cleanable;

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

    boolean runCommand(CommandSender sender, String commandLine);

    ConsoleCommandSender getConsoleSender();

    void logExecution(CommandSource sender, boolean ran, String alias, String args);

    void logTabCompletion(CommandSource sender, String alias, String args);

    CommandBuilder<BasicParametricCommand, CommandOrigin> getCommandBuilder();


    /**
     * Creates {@link de.cubeisland.engine.butler.parametric.BasicParametricCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param module        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    void addCommands(Dispatcher dispatcher, Module module, Object commandHolder);
    void addCommands(Module module, Object commandHolder);
}
