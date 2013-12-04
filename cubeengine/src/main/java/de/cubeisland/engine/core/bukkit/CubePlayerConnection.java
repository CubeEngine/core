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
package de.cubeisland.engine.core.bukkit;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PlayerConnection;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.packethook.PacketEventManager;

// TODO FIXME Packets have changed name. They don't include the number anymore. And each packet has a In and Out version

/**
 * This class is used to replace the original NetServerHandler and calling an
 * Events when receiving packets.
 */
public class CubePlayerConnection extends PlayerConnection
{
    private final Player player;
    private final PlayerConnection oldPlayerConnection;
    private final PacketEventManager em;

    public CubePlayerConnection(Player bukkitPlayer, EntityPlayer player, PlayerConnection oldPlayerConnection)
    {
        super(player.server, player.playerConnection.networkManager, player);
        this.player = bukkitPlayer;
        this.oldPlayerConnection = oldPlayerConnection;
        BukkitCore core = (BukkitCore)CubeEngine.getCore();
        this.em = core.getPacketEventManager();
    }

    public PlayerConnection getOldPlayerConnection()
    {
        return oldPlayerConnection;
    }

    /**
     * handles received packets
     *
     * @param packet the packet
     * @return true if the packet should be dropped
     */
    public boolean packetReceived(final Packet packet)
    {
        // System.out.println("Received: " + packet.k());
        return this.em.fireReceivedEvent(this.player, packet);
    }

    @Override
    public void sendPacket(final Packet packet)
    {
        // System.out.println("Sent: " + packet.k());
        if (this.em.fireSentEvent(this.player, packet)) return;
        super.sendPacket(packet);
    }
}
