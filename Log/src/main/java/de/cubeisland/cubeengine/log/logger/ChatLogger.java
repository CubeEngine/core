package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.ChatConfig;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ChatLogger extends Logger<ChatConfig>
{
    public ChatLogger(Log module)
    {
        super(module, ChatConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        World world = event.getPlayer().getWorld();
        ChatConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (event.getMessage().trim().isEmpty())
                return;
            if (config.logPlayerCommand)
            {
                if (!config.ignoreRegex.isEmpty())
                {
                    for (String regex : config.ignoreRegex)
                    {
                        if (event.getMessage().matches(regex))
                        {
                            return;
                        }
                    }
                }
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                this.module.getLogManager().logChatLog(user.key.intValue(), world, user.getLocation(), event.getMessage(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        World world = event.getPlayer().getWorld();
        ChatConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (event.getMessage().trim().isEmpty())
                return;
            if (config.logPlayerChat)
            {
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                // creating a new Location instance to ensure thread safety
                this.module.getLogManager().logChatLog(user.key.intValue(), world, user.getLocation(), event.getMessage(), true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        if (event.getCommand().trim().isEmpty())
            return;
        ChatConfig config = this.module.getGlobalConfiguration().getSubLogConfig(this.getConfigClass());
        if (config.logConsoleCommand)
        {
            this.module.getLogManager().logChatLog(0, null, null, event.getCommand(), false);
        }
    }

}
