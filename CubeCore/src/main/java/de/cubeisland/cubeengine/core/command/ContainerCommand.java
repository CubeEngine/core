package de.cubeisland.cubeengine.core.command;

import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.module.Module;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public class ContainerCommand extends CubeCommand
{
    public ContainerCommand(Module module, String name, String description, List<String> aliases)
    {
        super(module, name, description, "[command]", aliases);
    }

    @Override
    public void run(CommandContext context)
    {
        CommandSender sender = context.getSender();
        sender.sendMessage("The Following commands are available:");
        sender.sendMessage(" ");


        for (CubeCommand command : context.getCommand().getChildren())
        {
            sender.sendMessage(command.getName() + _(sender, command.getModule().getName(), command.getDescription()));
        }
    }
}