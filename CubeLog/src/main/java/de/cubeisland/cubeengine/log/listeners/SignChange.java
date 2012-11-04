package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class SignChange extends LogListener
{
    public SignChange(Log module)
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
            this.actions.put(LogAction.SIGNTEXT, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "signchange";
        }
    }
}