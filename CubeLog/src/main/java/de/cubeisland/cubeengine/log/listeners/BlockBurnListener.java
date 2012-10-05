package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager.BlockBreakCause;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

/**
 *
 * @author Anselm Brehme
 */
public class BlockBurnListener extends LogListener
{
    public BlockBurnListener(Log module)
    {
        super(module, new BurnConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        lm.logBreakBlock(BlockBreakCause.FIRE, null, event.getBlock().getState());
    }

    public static class BurnConfig extends LogSubConfiguration
    {
        public BurnConfig()
        {
            this.actions.put(LogAction.FIRE, true);
        }
        @Option("burn-actions")
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "burn";
        }
    }
}
