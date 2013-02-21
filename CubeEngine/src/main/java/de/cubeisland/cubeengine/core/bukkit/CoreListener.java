package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.CubeEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

    public void onQuit(final PlayerQuitEvent event)
    {
        this.bukkitCore.getCommandManager().commandMap.lastCommandOffers.remove(event.getPlayer().getName());
    }
}
