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
package de.cubeisland.cubeengine.core.bukkit.packethook;

import net.minecraft.server.v1_6_R2.Packet;

import org.bukkit.entity.Player;

public abstract class PacketEvent
{
    private final Player player;
    private Packet packet;
    private boolean cancelled;

    public PacketEvent(Player player, Packet packet)
    {
        this.player = player;
        this.packet = packet;
        this.cancelled = false;
    }

    /**
     * Returns the user of this event
     *
     * @return the user
     */
    public Player getPlayer()
    {
        return player;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    /**
     * Returns the packet of this event
     *
     * @return the packet
     */
    public Packet getPacket()
    {
        return this.packet;
    }

    public void setPacket(Packet newPacket)
    {
        if (newPacket == null)
        {
            throw new IllegalArgumentException("The packet must not be null!");
        }
        if (!this.getPacket().getClass().isAssignableFrom(newPacket.getClass()))
        {
            throw new IllegalArgumentException("Must be of the same type!");
        }
        this.packet = newPacket;
    }

    /**
     * Returns the ID of the packet
     *
     * @return the packet ID
     */
    public int getPacketId()
    {
        return this.getPacket().n();
    }
}
