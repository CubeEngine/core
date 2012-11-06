package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractionLogger extends Logger<InteractionLogger.InteractionConfig>
{
    public InteractionLogger()
    {
        super(LogAction.INTERACTION);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        //TODO
//        DOORINTERACT
//     SWITCHINTERACT
//     CAKEEAT
//     NOTEBLOCKINTERACT
//     DIODEINTERACT
    }

    public static class InteractionConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "chat";
        }
    }
}