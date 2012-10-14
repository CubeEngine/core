package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager.BlockChangeCause;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

public class BlockBurn extends LogListener
{
    public BlockBurn(Log module)
    {
        super(module, new BurnConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        lm.logBreakBlock(BlockChangeCause.FIRE, null, event.getBlock().getState());
    }

    public static class BurnConfig extends LogSubConfiguration
    {
        public BurnConfig()
        {
            this.actions.put(LogAction.FIRE, true);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "burn";
        }
    }
}
