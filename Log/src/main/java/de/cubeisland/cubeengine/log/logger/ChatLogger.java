package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.LogManager;
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
        User user = Logger.module.getUserManager().getExactUser(event.getPlayer());
        LogManager.logChatLog(user.key, event.getMessage(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        User user = Logger.module.getUserManager().getExactUser(event.getPlayer());
        LogManager.logChatLog(user.key, event.getMessage(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        LogManager.logChatLog(0, event.getCommand(), false);
    }

    //TODO config for logging console / chat / commands
    //TODO config for ignoring commands matching a regex e.g. setting passwords or private messages
    public static class ChatConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "chat";
        }
    }
}
