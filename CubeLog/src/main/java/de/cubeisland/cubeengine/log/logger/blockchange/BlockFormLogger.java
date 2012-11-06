package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.log.logger.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import static de.cubeisland.cubeengine.log.logger.blockchange.BlockLogger.BlockChangeCause.FORM;

public class BlockFormLogger extends BlockLogger<BlockFormLogger.BlockFormConfig>
{
    public BlockFormLogger()
    {
        this.config = new BlockFormConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        this.logBlockChange(FORM, null, event.getBlock().getState(), event.getNewState());
    }

    public static class BlockFormConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "block-form";
        }
    }
}
