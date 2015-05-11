package de.cubeisland.engine.module.core.command.sender;

import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.spongepowered.api.util.command.source.ConsoleSource;

public class ConsoleCommandSender extends WrappedCommandSender<ConsoleSource>
{
    public ConsoleCommandSender(CoreModule core)
    {
        super(core, core.getGame().getServer().getConsole());
    }
}
