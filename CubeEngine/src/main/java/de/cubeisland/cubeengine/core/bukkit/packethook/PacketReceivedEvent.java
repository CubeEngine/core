package de.cubeisland.cubeengine.core.bukkit.packethook;

import net.minecraft.server.v1_5_R1.Packet;
import org.bukkit.entity.Player;

public class PacketReceivedEvent extends PacketEvent
{
    public PacketReceivedEvent(Player player, Packet packet)
    {
        super(player, packet);
    }
}
