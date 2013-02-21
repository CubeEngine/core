package de.cubeisland.cubeengine.core.bukkit.packethook;

import de.cubeisland.cubeengine.core.util.Cleanable;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.server.v1_4_R1.Packet;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

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
            this.logger.log(DEBUG, "The player was null!");
            return false;
        }
        if (packet == null)
        {
            this.logger.log(DEBUG, "The packet was null!");
            return false;
        }
        List<PacketReceivedListener> listeners = this.receivedListeners.get(packet.k());
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
            this.logger.log(DEBUG, "The player was null!");
            return false;
        }
        if (packet == null)
        {
            this.logger.log(DEBUG, "The packet was null!");
            return false;
        }
        List<PacketSentListener> listeners = this.sentListeners.get(packet.k());
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
