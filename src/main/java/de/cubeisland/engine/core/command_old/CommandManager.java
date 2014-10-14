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
package de.cubeisland.engine.core.command_old;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandBuilder;
import de.cubeisland.engine.command.Dispatcher;
import de.cubeisland.engine.command.completer.Completer;
import de.cubeisland.engine.command.methodic.BasicMethodicCommand;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.command.CommandOrigin;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command_old.result.confirm.ConfirmManager;
import de.cubeisland.engine.core.command_old.result.paginated.PaginationManager;
import de.cubeisland.engine.core.command_old.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Cleanable;

/**
 * This class manages the registration of commands.
 */
public interface CommandManager extends Cleanable, Dispatcher
{
    ReaderManager getReaderManager();

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

    void logExecution(CommandSender sender, CommandBase cubeCommand, String[] args);

    void logTabCompletion(CommandSender sender, CommandBase cubeCommand, String[] args);

    ConfirmManager getConfirmManager();

    PaginationManager getPaginationManager();

    /**
     * Returns a completer for the first registered class
     */
    Completer getDefaultCompleter(Class... types);

    /**
     * Registers a completer for given classes
     */
    void registerDefaultCompleter(Completer completer, Class... types);

    CommandBuilder<BasicMethodicCommand, CommandOrigin> getCommandBuilder();


    /**
     * Creates {@link de.cubeisland.engine.command.methodic.BasicMethodicCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param module        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    void addCommands(Dispatcher dispatcher, Module module, Object commandHolder);
}