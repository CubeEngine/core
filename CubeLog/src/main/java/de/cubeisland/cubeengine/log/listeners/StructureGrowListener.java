package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

/**
 *
 * @author Anselm Brehme
 */
public class StructureGrowListener extends LogListener
{
    public StructureGrowListener(Log module)
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
        @Option("actions")
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "grow";
        }
    }
}