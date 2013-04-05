package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class PlayerChat extends SimpleLogActionType
{
    public PlayerChat(Log module)
    {
        super(module, 0xA2, "player-chat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getMessage().trim().isEmpty()) return;
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getMessage());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }
}
