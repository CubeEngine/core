package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * chatting
 * <p>Events: {@link AsyncPlayerChatEvent}</p>
 */
public class PlayerChat extends SimpleLogActionType
{
    public PlayerChat(Log module)
    {
        super(module, true, PLAYER);
    }

    @Override
    public String getName()
    {
        return "player-chat";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        //TODO attach spamming same msg
        if (event.getMessage().trim().isEmpty()) return;
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getMessage());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a chatted the following%s&a: &f\"&6%s&f\"",
                            time,logEntry.getCauserUser().getDisplayName(), loc,
                            logEntry.getAdditional().iterator().next().asText());
    }


    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.additional.iterator().next().asText().equals(other.additional.iterator().next().asText());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_CHAT_enable;
    }
}
