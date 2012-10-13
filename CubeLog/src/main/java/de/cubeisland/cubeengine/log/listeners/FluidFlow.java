package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

public class FluidFlow extends LogListener
{
    public FluidFlow(Log module)
    {
        super(module, new FluidConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        //TODO
    }

    public static class FluidConfig extends LogSubConfiguration
    {
        public FluidConfig()
        {
            this.actions.put(LogAction.LAVAFLOW, false);
            this.actions.put(LogAction.WATERFLOW, false);
            this.enabled = false;
        }
        @Option(value = "actions", genericType = Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "fluid";
        }
    }
}