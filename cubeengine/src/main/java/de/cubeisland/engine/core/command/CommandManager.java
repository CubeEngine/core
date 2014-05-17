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
package de.cubeisland.engine.core.command;

import de.cubeisland.engine.core.command.result.paginated.PaginationManager;

import de.cubeisland.engine.core.command.result.confirm.ConfirmManager;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Cleanable;

/**
 * This class manages the registration of commands.
 */
public interface CommandManager extends Cleanable
{
    /**
     * Registers a command
     *
     * @param command the command to register
     * @param parents the path under which the command should be registered
     */
    void registerCommand(CubeCommand command, String... parents);
    void registerCommands(Module module, CommandHolder commandHolder, String... parents);

    /**
     * Registers all methods annotated as a command in the given command holder object
     *
     * @param module        the module to register them for
     * @param commandHolder the command holder containing the commands
     * @param parents       the path under which the command should be registered
     */
    void registerCommands(Module module, Object commandHolder, Class<? extends CubeCommand> commandType, String... parents);

    /**
     * Gets a CubeCommand by its name
     *
     * @param name the name
     * @return the CubeCommand instance or null if not found
     */
    CubeCommand getCommand(String name);

    /**
     * Removes a command by its name
     *
     * @param name the name of the command to remove
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

    <T extends CubeCommand> void registerCommandFactory(CommandFactory<T> factory);
    CommandFactory getCommandFactory(Class<? extends CubeCommand> type);
    void removeCommandFactory(Class type);

    void logExecution(CommandSender sender, CubeCommand cubeCommand, String[] args);
    void logTabCompletion(CommandSender sender, CubeCommand cubeCommand, String[] args);

    ConfirmManager getConfirmManager();

    PaginationManager getPaginationManager();
}
