package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

/**
 *
 * @author Anselm Brehme
 */
public class BlockForm extends LogListener
{
    public BlockForm(Log module)
    {
        super(module, new FormConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        //TODO
    }

    public static class FormConfig extends LogSubConfiguration
    {
        public FormConfig()
        {
            this.actions.put(LogAction.SNOWFORM, false);
            this.actions.put(LogAction.ICEFORM, false);
            this.enabled = false;
        }
        @Option(value="actions",genericType=Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "form";
        }
    }
}