package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;

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
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_COMMAND,event.getMessage())) return;
        ArrayNode json = this.module.getObjectMapper().createArrayNode();
        json.add(event.getMessage());
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), PLAYER_COMMAND, event.getPlayer(),json.toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_CHAT)) return;
        if (event.getMessage().trim().isEmpty()) return;
        ArrayNode json = this.module.getObjectMapper().createArrayNode();
        json.add(event.getMessage());
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), PLAYER_CHAT, event.getPlayer(),json.toString());
    }
}
