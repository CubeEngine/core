package de.cubeisland.engine.core.bukkit.packethook;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.PlayerConnection;
import net.minecraft.server.v1_6_R2.ServerConnection;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.CubePlayerConnection;
import de.cubeisland.engine.core.util.ReflectionUtils;

public final class PacketHookInjector implements Listener
{
    private static PacketHookInjector instance = null;

    private Field playerConnectionListField;

    private PacketHookInjector()
    {
        this.playerConnectionListField = ReflectionUtils.findFirstField(ServerConnection.class, List.class);
        this.playerConnectionListField.setAccessible(true);
    }

    public synchronized static PacketHookInjector getInstance()
    {
        return instance;
    }

    /**
     * Registers the packet hook injector
     *
     * @param core the BukkitCore
     */
    public static synchronized boolean register(BukkitCore core)
    {
        if (instance == null)
        {
            try
            {
                instance = new PacketHookInjector();
                core.getServer().getPluginManager().registerEvents(instance, core);

                for (Player player : core.getServer().getOnlinePlayers())
                {
                    instance.replacePlayerConnection(player);
                }
            }
            catch (Exception e)
            {
                core.getLog().error(e.getLocalizedMessage(), e);
                return false;
            }
        }
        return true;
    }

    public synchronized static void shutdown()
    {
        if (instance != null)
        {
            HandlerList.unregisterAll(instance);

            for (Player player : Bukkit.getOnlinePlayers())
            {
                instance.resetPlayerConnection(player);
            }

            instance = null;
        }
    }

    /**
     * The event listener swaps the joining player's NetServerHandler
     * instance with a custom one including all the magic to make the new
     * NetServerHandler work.
     *
     * @param event the join event object
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        replacePlayerConnection(event.getPlayer());
    }

    public void replacePlayerConnection(final Player player)
    {
        final EntityPlayer entity = ((CraftPlayer)player).getHandle();

        swapPlayerConnection(entity, new CubePlayerConnection(player, entity, entity.playerConnection));
    }

    public void resetPlayerConnection(Player player)
    {
        final EntityPlayer entity = ((CraftPlayer)player).getHandle();

        // only swap back if it's still our wrapper
        if (entity.playerConnection instanceof CubePlayerConnection)
        {
            PlayerConnection old = ((CubePlayerConnection)entity.playerConnection).getOldPlayerConnection();
            old.disconnected = false;
            swapPlayerConnection(entity, old);
        }
    }

    @SuppressWarnings("unchecked")
    private void swapPlayerConnection(EntityPlayer player, PlayerConnection newHandler)
    {
        if (playerConnectionListField == null)
        {
            return;
        }
        PlayerConnection oldHandler = player.playerConnection;
        try
        {
            if (oldHandler.getClass() != newHandler.getClass())
            {
                Location loc = player.getBukkitEntity().getLocation();
                newHandler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

                ServerConnection serverConnection = player.server.ag();
                ((List<PlayerConnection>)playerConnectionListField.get(serverConnection)).remove(oldHandler);
                serverConnection.a(newHandler);
                CubeEngine.getLog().debug("Replaced the PlayerConnection of player '{}'", player.getName());
                oldHandler.disconnected = true;
            }
        }
        catch (Exception e)
        {
            player.playerConnection = oldHandler;
            CubeEngine.getLog().debug("Failed to swap the PlayerConnection of player {}: " + e.getLocalizedMessage(), e);
        }
    }
}
