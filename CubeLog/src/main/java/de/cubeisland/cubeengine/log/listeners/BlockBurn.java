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
public class BlockBurn extends LogListener
{
    public BlockBurn(Log module)
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
            this.enabled = false;
        }
        @Option(value="actions",genericType=Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "burn";
        }
    }
}
