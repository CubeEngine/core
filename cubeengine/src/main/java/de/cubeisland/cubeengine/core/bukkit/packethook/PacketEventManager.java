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

import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.v1_5_R3.Packet;
import net.minecraft.server.v1_5_R3.Packet204LocaleAndViewDistance;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.util.Cleanable;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;



public class PacketEventManager implements Cleanable
{
    private final Logger logger;
    private final TIntObjectHashMap<List<PacketReceivedListener>> receivedListeners;
    private final TIntObjectHashMap<List<PacketSentListener>> sentListeners;

    public PacketEventManager(Logger logger)
    {
        this.logger = logger;
        this.receivedListeners = new TIntObjectHashMap<List<PacketReceivedListener>>();
        this.sentListeners = new TIntObjectHashMap<List<PacketSentListener>>();
        this.addReceivedListener(204, new PacketReceivedListener()
        {
            @Override
            public void handle(PacketReceivedEvent event)
            {
                Bukkit.getPluginManager().callEvent(new PlayerLanguageReceivedEvent(event.getPlayer(), ((Packet204LocaleAndViewDistance)event.getPacket()).d()));
            }
        });
    }

    public void addReceivedListener(int packetId, PacketReceivedListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener must not be null!");
        }
        if (packetId < 0 || packetId > 255)
        {
            throw new IllegalArgumentException("Packet IDs have to be between 0 and 255!");
        }
        List<PacketReceivedListener> listeners = this.receivedListeners.get(packetId);
        if (listeners == null)
        {
            this.receivedListeners.put(packetId, listeners = new LinkedList<PacketReceivedListener>());
        }
        listeners.add(listener);
    }

    public void addSentListener(int packetId, PacketSentListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener must not be null!");
        }
        if (packetId < 0 || packetId > 255)
        {
            throw new IllegalArgumentException("Packet IDs have to be between 0 and 255!");
        }
        List<PacketSentListener> listeners = this.sentListeners.get(packetId);
        if (listeners == null)
        {
            this.sentListeners.put(packetId, listeners = new LinkedList<PacketSentListener>());
        }
        listeners.add(listener);
    }

    public void removeListener(int packetId, PacketReceivedListener listener)
    {
        List<PacketReceivedListener> listeners = this.receivedListeners.get(packetId);
        if (listeners == null)
        {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            this.receivedListeners.remove(packetId);
        }
    }

    public void removeListener(int packetId, PacketSentListener listener)
    {
        List<PacketSentListener> listeners = this.sentListeners.get(packetId);
        if (listeners == null)
        {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            this.sentListeners.remove(packetId);
        }
    }

    public boolean fireReceivedEvent(Player player, Packet packet)
    {
        if (player == null)
        {
            this.logger.debug("The player was null!");
            return false;
        }
        if (packet == null)
        {
            this.logger.debug("The packet was null!");
            return false;
        }
        List<PacketReceivedListener> listeners = this.receivedListeners.get(packet.n());
        if (listeners == null)
        {
            return false;
        }
        PacketReceivedEvent event = new PacketReceivedEvent(player, packet);
        for (PacketReceivedListener listener : listeners)
        {
            listener.handle(event);
        }
        return event.isCancelled();
    }

    public boolean fireSentEvent(Player player, Packet packet)
    {
        if (player == null)
        {
            this.logger.debug("The player was null!");
            return false;
        }
        if (packet == null)
        {
            this.logger.debug("The packet was null!");
            return false;
        }
        List<PacketSentListener> listeners = this.sentListeners.get(packet.n());
        if (listeners == null)
        {
            return false;
        }
        PacketSentEvent event = new PacketSentEvent(player, packet);
        for (PacketSentListener listener : listeners)
        {
            listener.handle(event);
        }
        return event.isCancelled();
    }

    @Override
    public void clean()
    {
        this.sentListeners.clear();
        this.receivedListeners.clear();
    }
}
