package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 *
 * @author Anselm Brehme
 */
public class EndermanListener extends LogListener
{
    public EndermanListener(Log module)
    {
        super(module, new EndermanConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if (event.getEntity() instanceof Enderman)
        {
            BlockState newState = event.getBlock().getState();
            newState.setType(event.getTo());
            lm.logEnderGrief(event.getBlock().getState(), newState);
        }
    }

    public static class EndermanConfig extends LogSubConfiguration
    {
        public EndermanConfig()
        {
            this.actions.put(LogAction.ENDERMEN, false);
            this.enabled = false;
        }
        @Option(value="actions",genericType=Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "enderman";
        }
    }
}
