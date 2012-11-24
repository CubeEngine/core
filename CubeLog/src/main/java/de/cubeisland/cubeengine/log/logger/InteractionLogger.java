package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractionLogger extends
    Logger<InteractionLogger.InteractionConfig>
{
    public InteractionLogger()
    {
        super(LogAction.INTERACTION);
        this.config = new InteractionConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            switch (event.getClickedBlock().getType())
            {
                //TODO
                case WOODEN_DOOR:
                case TRAP_DOOR:
                case FENCE_GATE:
                case LEVER:
                case STONE_BUTTON:
                case WOOD_BUTTON:
                case CAKE_BLOCK:
                case NOTE_BLOCK:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
            }
        }
        //TODO open door via pressure plate ?
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
