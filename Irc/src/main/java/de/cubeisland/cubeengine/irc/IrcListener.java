package de.cubeisland.cubeengine.irc;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class IrcListener implements Listener
{
    private final Irc         irc;
    private final TaskManager taskmgr;
    private final BotManager  botmgr;

    public IrcListener(Irc irc)
    {
        this.irc = irc;
        this.taskmgr = irc.getTaskManger();
        this.botmgr = irc.getBotManager();
    }

    public void onJoin(PlayerJoinEvent event)
    {
        this.botmgr.addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event)
    {
        this.taskmgr.scheduleAsyncDelayedTask(this.irc, new Runnable()
        {
            @Override
            public void run()
            {
                if (!event.getPlayer().isOnline())
                {
                    botmgr.removePlayer(event.getPlayer());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event)
    {
        this.botmgr.sendMessage(event.getPlayer(), event.getMessage());
    }
}
