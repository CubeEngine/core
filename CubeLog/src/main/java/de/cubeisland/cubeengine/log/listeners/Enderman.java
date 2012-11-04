package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class Enderman extends LogListener
{
    public Enderman(Log module)
    {
        super(module, new EndermanConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if (event.getEntityType().equals(EntityType.ENDERMAN))
        {
            BlockState newState = event.getBlock().getState();
            newState.setType(event.getTo());
            lm.logChangeBlock(LogManager.BlockChangeCause.ENDERMAN, null, event.getBlock().getState(), newState);
        }
    }

    public static class EndermanConfig extends LogSubConfiguration
    {
        public EndermanConfig()
        {
            this.actions.put(LogAction.ENDERMEN, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "enderman";
        }
    }
}
