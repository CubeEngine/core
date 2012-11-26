package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import java.util.ArrayList;
import java.util.List;
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
        if (this.config.logPlayerCommand)
        {
            if (!this.config.ignoreRegex.isEmpty())
            {
                for (String regex : this.config.ignoreRegex)
                {
                    if (event.getMessage().matches(regex))
                    {
                        return;
                    }
                }
            }
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            this.module.getLogManager().logChatLog(user.key, user.getLocation(), event.getMessage(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (this.config.logPlayerChat)
        {
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            this.module.getLogManager().logChatLog(user.key, user.getLocation(), event.getMessage(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        if (this.config.logConsoleCommand)
        {
            this.module.getLogManager().logChatLog(0, null, event.getCommand(), false);
        }
    }

    public static class ChatConfig extends SubLogConfig
    {
        @Option("log-console-command")
        public boolean logConsoleCommand = true;
        @Option("log-player-command")
        public boolean logPlayerCommand = true;
        @Option("log-player-chat")
        public boolean logPlayerChat = true;
        @Comment("Regex of commands to ignore when logging player commands.")
        @Option("ignore-commands")
        public List<String> ignoreRegex = new ArrayList<String>();//TODO add default CE pw setting


        @Override
        public String getName()
        {
            return "chat";
        }
    }
}
