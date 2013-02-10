package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.event.PacketReceivedEvent;
import de.cubeisland.cubeengine.core.bukkit.event.PlayerLanguageReceivedEvent;
import net.minecraft.server.v1_4_R1.Packet204LocaleAndViewDistance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class CoreListener implements Listener
{
    private final BukkitCore bukkitCore;
    private final PluginManager pm;
    private final BukkitScheduler scheduler;
    private final CoreConfiguration config;

    CoreListener(Core core)
    {
        this.bukkitCore = (BukkitCore)core;

        this.pm = this.bukkitCore.getServer().getPluginManager();
        this.scheduler = this.bukkitCore.getServer().getScheduler();
        this.config = core.getConfiguration();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        this.scheduler.scheduleSyncDelayedTask(this.bukkitCore, new Runnable()
        {
            @Override
            public void run()
            {
                AfterJoinEvent afterJoinEvent = new AfterJoinEvent(event.getPlayer(), event.getJoinMessage());
                CubeEngine.getEventManager().fireEvent(afterJoinEvent);
            }
        }, config.afterJoinEventDelay);
    }

    @EventHandler
    public void onPacket(PacketReceivedEvent event)
    {
        if (event.getPacket() instanceof Packet204LocaleAndViewDistance)
        {
            Packet204LocaleAndViewDistance packet = (Packet204LocaleAndViewDistance)event.getPacket();
            this.pm.callEvent(new PlayerLanguageReceivedEvent(event.getPlayer(), packet.d()));
        }
    }
}
