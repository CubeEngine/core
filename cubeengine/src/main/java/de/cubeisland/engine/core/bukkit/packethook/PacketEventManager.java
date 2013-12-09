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
package de.cubeisland.engine.core.bukkit.packethook;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayInSettings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.engine.core.util.Cleanable;
import de.cubeisland.engine.logging.Log;

// TODO FIXME "Packet 204 - Locale and View Distance" has been replaced with "Packet 0x15 - Client Settings"

public class PacketEventManager implements Cleanable
{
    private final Log logger;
    private final Map<Class<? extends Packet>, List<PacketReceivedListener>> receivedListeners;
    private final Map<Class<? extends Packet>, List<PacketSentListener>> sentListeners;

    public PacketEventManager(Log logger)
    {
        this.logger = logger;
        this.receivedListeners = new HashMap<>();
        this.sentListeners = new HashMap<>();
        this.addReceivedListener(PacketPlayInSettings.class, new PacketReceivedListener()
        {
            @Override
            public void handle(PacketReceivedEvent event)
            {
                Bukkit.getPluginManager().callEvent(new PlayerLanguageReceivedEvent(event.getPlayer(), ((PacketPlayInSettings)event.getPacket()).c()));
            }
        });
    }

    public void addReceivedListener(Class<? extends Packet> clazz, PacketReceivedListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener must not be null!");
        }
        List<PacketReceivedListener> listeners = this.receivedListeners.get(clazz);
        if (listeners == null)
        {
            this.receivedListeners.put(clazz, listeners = new LinkedList<>());
        }
        listeners.add(listener);
    }

    public void addSentListener(Class<? extends Packet> clazz, PacketSentListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener must not be null!");
        }
        List<PacketSentListener> listeners = this.sentListeners.get(clazz);
        if (listeners == null)
        {
            this.sentListeners.put(clazz, listeners = new LinkedList<>());
        }
        listeners.add(listener);
    }

    public void removeListener(Class<? extends Packet> clazz, PacketReceivedListener listener)
    {
        List<PacketReceivedListener> listeners = this.receivedListeners.get(clazz);
        if (listeners == null)
        {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            this.receivedListeners.remove(clazz);
        }
    }

    public void removeListener(Class<? extends Packet> clazz, PacketSentListener listener)
    {
        List<PacketSentListener> listeners = this.sentListeners.get(clazz);
        if (listeners == null)
        {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            this.sentListeners.remove(clazz);
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
        List<PacketReceivedListener> listeners = this.receivedListeners.get(packet.getClass());
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
        List<PacketSentListener> listeners = this.sentListeners.get(packet.getClass());
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
