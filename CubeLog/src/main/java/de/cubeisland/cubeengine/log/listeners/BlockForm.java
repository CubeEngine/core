package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

public class BlockForm extends LogListener
{
    public BlockForm(Log module)
    {
        super(module, new FormConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        lm.logChangeBlock(LogManager.BlockChangeCause.FORM, null, event.getBlock().getState(), event.getNewState());
    }

    public static class FormConfig extends LogSubConfiguration
    {
        public FormConfig()
        {
            this.actions.put(LogAction.SNOWFORM, false);
            this.actions.put(LogAction.ICEFORM, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "form";
        }
    }
}