package de.cubeisland.cubeengine.core.command.sender;

public class ConsoleCommandSender extends WrappedCommandSender
{
    public ConsoleCommandSender(org.bukkit.command.ConsoleCommandSender sender)
    {
        super(sender);
    }
}
