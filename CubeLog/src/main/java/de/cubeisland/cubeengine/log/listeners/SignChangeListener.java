package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Anselm Brehme
 */
public class SignChangeListener extends LogListener
{
    public SignChangeListener(Log module)
    {
        super(module, new SignChangeConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        //TODO
    }

    public static class SignChangeConfig extends LogSubConfiguration
    {
        public SignChangeConfig()
        {
            this.actions.put(LogAction.LAVAFLOW, false);
            this.actions.put(LogAction.WATERFLOW, false);
        }
        @Option("actions")
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "signchange";
        }
    }
}