package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ChatLogger extends Logger<ChatLogger.ChatConfig>
{
    public ChatLogger()
    {
        super(LogAction.CHAT);
        this.config = new ChatConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        //TODO
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        //TODO
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        //TODO
    }

    public static class ChatConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "chat";
        }
    }
}
