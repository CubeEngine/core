package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractionLogger extends Logger<InteractionLogger.InteractionConfig>
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
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            switch (event.getClickedBlock().getType())
            {
                //TODO
                case WOODEN_DOOR: //open close
                case TRAP_DOOR://open close
                case FENCE_GATE://open close
                case LEVER://on off
                case STONE_BUTTON:
                case WOOD_BUTTON:
                case CAKE_BLOCK://eat newData
                case NOTE_BLOCK://changed newData
                case DIODE_BLOCK_OFF://changed newData
                case DIODE_BLOCK_ON://changed newData
                    this.lm.logInteractLog(user.key, user.getLocation(), event.getClickedBlock().getType(), null);//TODO
            }
        }
        //TODO open door via pressure plate ?
    }

    public static class InteractionConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "interact";
        }
    }
}
