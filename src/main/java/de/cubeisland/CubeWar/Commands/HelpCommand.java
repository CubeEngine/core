package de.cubeisland.CubeWar.Commands;

import static de.cubeisland.CubeWar.CubeWar.t;
import org.bukkit.command.CommandSender;

/**
 * This command prints a help message
 *
 * @author Phillip Schichtel
 * @author Faithcaio
 */
public class HelpCommand extends AbstractCommand
{
    public HelpCommand(BaseCommand base)
    {
        super(base, "help");
    }

    public boolean execute(CommandSender sender, CommandArgs args)
    {
        for (AbstractCommand command : getBase().getRegisteredCommands())
        {
            sender.sendMessage(command.getUsage());
            sender.sendMessage("    " + command.getDescription());
        }
        sender.sendMessage("");
        return true;
    }
    
    private boolean check(AbstractCommand command, String... label)
    {
        int max = label.length;
        for (int i=0;i<max;i++)
            if (command.getLabel().equalsIgnoreCase(label[i])) return true;
        return false;
    }

    @Override
    public String getDescription()
    {
        return t("command_help");
    }
}
