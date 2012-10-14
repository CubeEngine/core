package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager.BlockChangeCause;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
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
        lm.logChangeBlock(BlockChangeCause.FADE, null, event.getBlock().getState(), event.getNewState());
    }

    public static class FadeConfig extends LogSubConfiguration
    {
        public FadeConfig()
        {
            this.actions.put(LogAction.SNOWFADE, false);
            this.actions.put(LogAction.ICEFADE, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "fade";
        }
    }
}