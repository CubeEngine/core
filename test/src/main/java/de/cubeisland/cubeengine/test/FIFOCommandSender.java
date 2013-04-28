package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;

public class FIFOCommandSender extends ConsoleCommandSender
{
    private final FIFOInterface fifo;

    public FIFOCommandSender(FIFOInterface fifo, BukkitCore core)
    {
        super(core);
        this.fifo = fifo;
    }

    public FIFOInterface getFifo()
    {
        return fifo;
    }

    @Override
    public void sendMessage(String message)
    {
        super.sendMessage(message);
        fifo.writeMessage(message);
    }
}
