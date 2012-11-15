package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FORM;

public class BlockFormLogger extends BlockLogger<BlockFormLogger.BlockFormConfig>
{
    public BlockFormLogger()
    {
        this.config = new BlockFormConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if ((event.getNewState().getType().equals(Material.ICE) && this.config.logIceForm)
            || event.getNewState().getType().equals(Material.SNOW) && this.config.logSnowForm)
        {
            this.logBlockChange(FORM, null, event.getBlock().getState(), event.getNewState());
        }
    }

    public static class BlockFormConfig extends SubLogConfig
    {
        @Option(value = "log-snow-form")
        public boolean logSnowForm = false;
        @Option(value = "log-ice-form")
        public boolean logIceForm = false;

        @Override
        public String getName()
        {
            return "block-form";
        }
    }
}