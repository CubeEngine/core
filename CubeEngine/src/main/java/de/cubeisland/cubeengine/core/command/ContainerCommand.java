package de.cubeisland.cubeengine.core.command;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.module.Module;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * This class is just a container for other commands that provides a help command
 */
public class ContainerCommand extends CubeCommand
{
    public ContainerCommand(Module module, String name, String description)
    {
        super(module, name, description, "[command]", Collections.<String>emptyList());
    }
    
    public ContainerCommand(Module module, String name, String description, List<String> aliases)
    {
        super(module, name, description, "[command]", aliases);
    }

    @Override
    public void run(CommandContext context)
    {
        this.showHelp(context);
    }

    @Override
    public void showHelp(CommandContext context)
    {
        CommandSender sender = context.getSender();
        context.sendMessage("core", "The Following commands are available:");
        context.sendMessage(" ");


        for (CubeCommand command : context.getCommand().getChildren())
        {
            context.sendMessage(command.getName() + _(sender, command.getModule(), command.getDescription()));
        }
    }
}