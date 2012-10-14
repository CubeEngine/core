package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

public class StructureGrow extends LogListener
{
    public StructureGrow(Log module)
    {
        super(module, new StructureGrowConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        //TODO
    }

    public static class StructureGrowConfig extends LogSubConfiguration
    {
        public StructureGrowConfig()
        {
            this.actions.put(LogAction.NATURALSTRUCTUREGROW, false);
            this.actions.put(LogAction.BONEMEALSTRUCTUREGROW, false);
            this.enabled = true;
        }

        @Override
        public String getName()
        {
            return "grow";
        }
    }
}