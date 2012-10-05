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

/**
 *
 * @author Anselm Brehme
 */
public class BlockFadeListener extends LogListener
{
    public BlockFadeListener(Log module)
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
        }
        @Option("actions")
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "fade";
        }
    }
}