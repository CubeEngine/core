package de.cubeisland.cubeengine.core.bukkit.packethook;

import net.minecraft.server.v1_5_R2.Packet;
import org.bukkit.entity.Player;

public class PacketSentEvent extends PacketEvent
{
    public PacketSentEvent(Player player, Packet packet)
    {
        super(player, packet);
    }
}
