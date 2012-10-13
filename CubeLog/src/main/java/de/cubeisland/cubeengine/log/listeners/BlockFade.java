package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFade extends LogListener
{
    public BlockFade(Log module)
    {
        super(module, new FadeConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        //TODO
    }

    public static class FadeConfig extends LogSubConfiguration
    {
        public FadeConfig()
        {
            this.actions.put(LogAction.SNOWFADE, false);
            this.actions.put(LogAction.ICEFADE, false);
            this.enabled = false;
        }
        @Option(value = "actions", genericType = Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "fade";
        }
    }
}