package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Cleanable;

/**
 * This class manages the registration of commands.
 */
public interface CommandManager extends Cleanable
{
    void registerCommand(CubeCommand command, String... parents);
    void registerCommands(Module module, CommandHolder commandHolder, String... parents);
    void registerCommands(Module module, Object commandHolder, Class<? extends CubeCommand> commandType, String... parents);
    void registerCommandFactory(CommandFactory factory);
    CommandFactory getCommandFactory(Class<? extends CubeCommand> type);
    void removeCommandFactory(Class clazz);
    CubeCommand getCommand(String name);
    void removeCommands(String name);
    void removeCommands(Module module);
    boolean runCommand(CommandSender sender, String commandLine);
    void removeCommands();
}
