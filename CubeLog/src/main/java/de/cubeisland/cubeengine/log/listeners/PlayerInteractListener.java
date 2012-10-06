package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Anselm Brehme
 */
public class PlayerInteractListener extends LogListener
{
    public PlayerInteractListener(Log module)
    {
        super(module, new InteractConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        //TODO
    }

    public static class InteractConfig extends LogSubConfiguration
    {
        public InteractConfig()
        {
            this.actions.put(LogAction.DOORINTERACT, false);
            this.actions.put(LogAction.SWITCHINTERACT, false);
            this.actions.put(LogAction.CAKEEAT, false);
            this.actions.put(LogAction.NOTEBLOCKINTERACT, false);
            this.actions.put(LogAction.DIODEINTERACT, false);
            this.enabled = false;
        }
        @Option(value="actions",genericType=Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "interact";
        }
    }
}