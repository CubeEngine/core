package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import static de.cubeisland.cubeengine.log.storage.LogManager.CONSOLE_COMMAND;
import static de.cubeisland.cubeengine.log.storage.LogManager.PLAYER_CHAT;
import static de.cubeisland.cubeengine.log.storage.LogManager.PLAYER_COMMAND;

public class ChatListener implements Listener
{
    private LogManager manager;
    private Log module;

    public ChatListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.getMessage().trim().isEmpty()) return;
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_COMMAND)) return;
        //TODO ignore regexes
        this.manager.queueLog(event.getPlayer().getLocation(),PLAYER_COMMAND,event.getPlayer(),event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_CHAT)) return;
        if (event.getMessage().trim().isEmpty()) return;
        this.manager.queueLog(event.getPlayer().getLocation(),PLAYER_CHAT,event.getPlayer(),event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        if (this.manager.isIgnored(null,CONSOLE_COMMAND)) return;
        //TODO ignore regexes
        this.manager.queueLog(CONSOLE_COMMAND,event.getCommand());
    }
}
